package net.pretronic.dkcoins.minecraft.commands;

import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.commands.account.AccountTopCommand;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
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

import java.util.Arrays;

public class UserBankCommand extends BasicCommand {

    private final ObjectCommand<Currency> topCommand;

    public UserBankCommand(ObjectOwner owner, CommandConfiguration configuration) {
        super(owner, configuration);
        this.topCommand = new AccountTopCommand(owner);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof MinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(((MinecraftPlayer)commandSender).getUniqueId());
        if(args.length == 0) {
            AccountCredit credit = user.getDefaultAccount().getCredit(getCurrency());
            commandSender.sendMessage(Messages.COMMAND_USER_BANK_AMOUNT, new ReflectVariableSet()
                    .add("credit", credit));
            return;
        }
        String action = args[0];
        if(action.equalsIgnoreCase("pay") || action.equalsIgnoreCase("transfer")) {
            if(args.length == 3) {
                AccountCredit credit = user.getDefaultAccount().getCredit(getCurrency());
                AccountMember member = user.getDefaultAccount().getMember(user);
                String receiver0 = args[1];
                BankAccount receiver = DKCoins.getInstance().getAccountManager().searchAccount(receiver0);
                if(receiver == null) {
                    commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS, VariableSet.create().add("name", receiver0));
                    return;
                }

                String amount0 = args[2];
                if(!GeneralUtil.isNumber(amount0)) {
                    commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", amount0));
                    return;
                }
                double amount = Double.parseDouble(amount0);

                TransferResult result = credit.transfer(member, amount, receiver.getCredit(credit.getCurrency()),
                        CommandUtil.buildReason(args, 3), TransferCause.TRANSFER,
                        DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
                if(result.isSuccess()) {
                    commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_SUCCESS, new ReflectVariableSet()
                            .add("transaction", result.getTransaction()));
                } else {
                    CommandUtil.handleTransferFailCauses(result, commandSender);
                }
            } else {
                commandSender.sendMessage(Messages.COMMAND_USER_BANK_HELP, VariableSet.create().add("currency", getCurrency().getName()));
            }
        } else if(action.equalsIgnoreCase("top")) {
            this.topCommand.execute(commandSender, getCurrency(), args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
        } else {
            DKCoinsUser target = DKCoins.getInstance().getUserManager().getUser(action);
            if(target == null) {
                commandSender.sendMessage(Messages.ERROR_USER_NOT_EXISTS, VariableSet.create().add("name", action));
                return;
            }
            AccountCredit credit = target.getDefaultAccount().getCredit(getCurrency());
            commandSender.sendMessage(Messages.COMMAND_USER_BANK_AMOUNT_OTHER, new ReflectVariableSet()
                    .add("other", target.getDefaultAccount().getMember(target))
                    .add("credit", credit));
        }
    }

    private Currency getCurrency() {
        return DKCoins.getInstance().getCurrencyManager().searchCurrency(getConfiguration().getName());
    }
}
