/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 04.02.20, 15:50
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands.account;

import net.prematic.libraries.command.command.ObjectCommand;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.utility.GeneralUtil;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.player.OnlineMinecraftPlayer;

import java.util.ArrayList;

public class AccountExchangeCommand extends ObjectCommand<BankAccount> {

    public AccountExchangeCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("exchange"));
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(args.length < 3) {
            commandSender.sendMessage(Messages.COMMAND_EXCHANGE_HELP);
            return;
        }
        String sourceCurrency0 = args[0];
        String destinationCurrency0 = args[1];
        String amount0 = args[2];

        Currency sourceCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(sourceCurrency0);
        if(sourceCurrency == null) {
            commandSender.sendMessage(Messages.COMMAND_EXCHANGE_HELP);
            return;
        }
        Currency destinationCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(destinationCurrency0);
        if(destinationCurrency == null) {
            commandSender.sendMessage(Messages.COMMAND_EXCHANGE_HELP);
            return;
        }
        if(!GeneralUtil.isNumber(amount0)) {
            commandSender.sendMessage(Messages.COMMAND_EXCHANGE_HELP);
            return;
        }
        double amount = Double.parseDouble(amount0);

        OnlineMinecraftPlayer player = (OnlineMinecraftPlayer)commandSender;
        AccountMember member = account.getMember(DKCoins.getInstance().getUserManager().getUser(player.getUniqueId()));

        boolean success = account.exchangeAccountCredit(member, sourceCurrency, destinationCurrency, amount,
                CommandUtil.buildReason(args, 3), DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
        if(success) {
            player.sendMessage(Messages.COMMAND_EXCHANGE_SUCCESS);
        } else {
            player.sendMessage(Messages.COMMAND_EXCHANGE_FAILURE);
        }
    }
}
