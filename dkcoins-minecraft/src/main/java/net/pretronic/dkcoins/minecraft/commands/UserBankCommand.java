package net.pretronic.dkcoins.minecraft.commands;

import net.prematic.libraries.command.command.BasicCommand;
import net.prematic.libraries.command.command.MainCommand;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.GeneralUtil;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.TransferResult;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.account.TransferCause;
import org.mcnative.common.player.MinecraftPlayer;

public class UserBankCommand extends BasicCommand {

    public UserBankCommand(ObjectOwner owner, CommandConfiguration configuration) {
        super(owner, configuration);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof MinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(((MinecraftPlayer)commandSender).getUniqueId());
        if(args.length == 0) {
            printInfoMessage(commandSender, user);
            return;
        }
        String action = args[0];
        if(action.equalsIgnoreCase("pay") || action.equalsIgnoreCase("transfer")) {
            if(args.length == 3) {
                AccountCredit credit = user.getDefaultAccount().getCredit(DKCoinsConfig.DEFAULT_CURRENCY);
                AccountMember member = user.getDefaultAccount().getMember(user);
                String receiver0 = args[1];
                BankAccount receiver = DKCoins.getInstance().getAccountManager().searchAccount(receiver0);
                if(receiver == null) {
                    commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS, VariableSet.create().add("name", receiver0));
                    return;
                }

                String amount0 = args[2];
                if(!GeneralUtil.isNumber(amount0)) {
                    commandSender.sendMessage(Messages.ERROR_NOT_NUMBER);
                    return;
                }
                double amount = Double.parseDouble(amount0);

                TransferResult result = credit.transfer(member, amount, receiver.getCredit(credit.getCurrency()),
                        CommandUtil.buildReason(args, 3), TransferCause.TRANSFER,
                        DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
                if(result.isSuccess()) {
                    commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_SUCCESS, VariableSet.create()
                            .add("amount", amount)
                            .add("currency", credit.getCurrency())
                            .add("receiver", receiver.getName()));
                } else {
                    CommandUtil.handleTransferFailCauses(result, commandSender);
                }
            } else {
                commandSender.sendMessage(Messages.COMMAND_USER_BANK_HELP, VariableSet.create().add("currency", getConfiguration().getName()));
            }
        } else {
            DKCoinsUser target = DKCoins.getInstance().getUserManager().getUser(action);
            if(target == null) {
                commandSender.sendMessage(Messages.ERROR_USER_NOT_EXISTS);
                return;
            }
            printInfoMessage(commandSender, target);
        }
    }

    private void printInfoMessage(CommandSender commandSender, DKCoinsUser user) {
        AccountCredit credit = user.getDefaultAccount().getCredit(DKCoinsConfig.DEFAULT_CURRENCY);
        commandSender.sendMessage(Messages.COMMAND_USER_BANK_AMOUNT, VariableSet.create()
                .add("amount", credit.getAmount()).add("currency_symbol", credit.getCurrency().getSymbol()));
    }
}
