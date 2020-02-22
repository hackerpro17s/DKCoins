/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 04.02.20, 15:29
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
import net.pretronic.dkcoins.minecraft.account.TransferCause;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.player.OnlineMinecraftPlayer;

import java.util.ArrayList;

public class AccountTransferCommand extends ObjectCommand<BankAccount> {

    public AccountTransferCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("transfer").aliases("pay").create());
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(args.length < 3) {
            commandSender.sendMessage(Messages.COMMAND_TRANSFER_HELP);
            return;
        }
        String receiver0 = args[0];
        String amount0 = args[1];
        String currency0 = args[2];

        BankAccount receiver = DKCoins.getInstance().getAccountManager().searchAccount(receiver0);
        if(receiver == null) {
            commandSender.sendMessage(Messages.COMMAND_TRANSFER_HELP);
            return;
        }
        if(!GeneralUtil.isNumber(amount0)) {
            commandSender.sendMessage(Messages.COMMAND_TRANSFER_HELP);
            return;
        }
        double amount = Double.parseDouble(amount0);

        Currency currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(currency0);
        if(currency == null) {
            commandSender.sendMessage(Messages.COMMAND_TRANSFER_HELP);
            return;
        }
        OnlineMinecraftPlayer player = (OnlineMinecraftPlayer)commandSender;
        AccountMember member = account.getMember(DKCoins.getInstance().getUserManager().getUser(player.getUniqueId()));

        boolean success = account.getCredit(currency).transfer(member, amount, receiver.getCredit(currency),
                CommandUtil.buildReason(args, 3), TransferCause.TRANSFER,
                DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
        if(success) {
            commandSender.sendMessage(Messages.COMMAND_TRANSFER_SUCCESS);
        } else {
            commandSender.sendMessage(Messages.COMMAND_TRANSFER_FAILURE);
        }
    }
}
