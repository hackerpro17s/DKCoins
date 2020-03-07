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

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.GeneralUtil;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.TransferResult;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.account.TransferCause;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.player.OnlineMinecraftPlayer;

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
            commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_HELP);
            return;
        }
        if(CommandUtil.hasAccessAndSendMessage(commandSender, account, AccessRight.WITHDRAW)) {
            String receiver0 = args[0];
            String amount0 = args[1];
            String currency0 = args[2];

            BankAccount receiver = DKCoins.getInstance().getAccountManager().searchAccount(receiver0);
            if(receiver == null) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS);
                return;
            }
            if(!GeneralUtil.isNumber(amount0)) {
                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER);
                return;
            }
            double amount = Double.parseDouble(amount0);

            Currency currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(currency0);
            if(currency == null) {
                commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS);
                return;
            }
            OnlineMinecraftPlayer player = (OnlineMinecraftPlayer) commandSender;
            AccountMember member = account.getMember(DKCoins.getInstance().getUserManager().getUser(player.getUniqueId()));

            TransferResult result = account.getCredit(currency).transfer(member, amount, receiver.getCredit(currency),
                    CommandUtil.buildReason(args, 3), TransferCause.TRANSFER,
                    DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
            if(result.isSuccess()) {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_SUCCESS, VariableSet.create()
                        .add("amount", amount)
                        .add("currency", currency.getName())
                        .add("receiver", receiver.getName()));
            } else {
                CommandUtil.handleTransferFailCauses(result, commandSender);
            }
        }
    }
}
