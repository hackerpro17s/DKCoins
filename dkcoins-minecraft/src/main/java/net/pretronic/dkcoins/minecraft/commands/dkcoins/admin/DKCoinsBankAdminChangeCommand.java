package net.pretronic.dkcoins.minecraft.commands.dkcoins.admin;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.common.account.DefaultBankAccount;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.Textable;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.Collection;
import java.util.Collections;

public class DKCoinsBankAdminChangeCommand extends ObjectCommand<BankAccount> implements Completable {

    private final Textable message;
    private final TransferExecution execution;

    public DKCoinsBankAdminChangeCommand(ObjectOwner owner, String commandName, Textable message, TransferExecution execution) {
        super(owner, CommandConfiguration.name(commandName));
        Validate.notNull(message, execution);
        this.message = message;
        this.execution = execution;
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(args.length < 1) {
            commandSender.sendMessage(Messages.COMMAND_DKCOINS_HELP);
            return;
        }
        String amount0 = args[0];
        String currency0 = args.length == 2 ? args[1] : null;

        double amount = CommandUtil.parseAmount(commandSender, amount0);
        if(amount >= 0) {
            Currency currency = CommandUtil.parseCurrency(commandSender, currency0);
            if(currency != null) {
                String reason = CommandUtil.buildReason(args, 2);
                AccountMember member = account.getMember(CommandUtil.getUserByCommandSender(commandSender));
                if(account.equals(DefaultBankAccount.DUMMY_ALL)) {
                    CommandUtil.loopThroughUserBanks(null, target -> change(commandSender, target, currency, member, amount, reason));
                } else {
                    change(commandSender, account, currency, member, amount, reason);
                }
            }
        }
    }

    private void change(CommandSender commandSender, BankAccount account, Currency currency, AccountMember member, double amount, String reason) {
        AccountTransaction transaction = this.execution.transfer(account, currency, member, amount, reason);
        commandSender.sendMessage(this.message, VariableSet.create()
                .addDescribed("transaction", transaction));
    }

    @Override
    public Collection<String> complete(CommandSender commandSender, String[] args) {
        if(args.length == 2) {
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName
                    ,currency -> currency.getName().toLowerCase().startsWith(args[1].toLowerCase()));
        }
        return Collections.emptyList();
    }

    public interface TransferExecution {

        AccountTransaction transfer(BankAccount account, Currency currency, AccountMember member, double amount, String reason);
    }
}
