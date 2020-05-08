package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.Collection;

public class BankStatementCommand extends ObjectCommand<BankAccount> {

    public BankStatementCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("bankStatement", "statement", "abstract"));
    }

    @Override
    public void execute(CommandSender sender, BankAccount account, String[] args) {
        TransactionFilter filter = TransactionFilter.newFilter().account(account);
        for (String filterOption : args) {
            if(!filterOption.contains("=")) {
                sender.sendMessage(Messages.COMMAND_BANK_BANK_STATEMENT_FILTER_OPTION_WRONG, VariableSet.create()
                        .add("filterOption", filterOption));
                return;
            }
            String[] split = filterOption.split("=");
            String key = split[0];
            String value = split[1];
            appendFilterOption(sender, filter, key, value);
        }
        Collection<AccountTransaction> transactions = DKCoins.getInstance().getAccountManager().filterAccountTransactions(filter);
        sender.sendMessage(Messages.COMMAND_BANK_BANK_STATEMENT, VariableSet.create()
                    .addDescribed("transactions", transactions));

    }

    private void appendFilterOption(CommandSender sender, TransactionFilter filter, String key, String value) {
        switch (key.toLowerCase()) {
            case "world": {
                filter.world(value);
                return;
            }
            case "server": {
                filter.server(value);
                return;
            }
            case "time": {
                if(!GeneralUtil.isNaturalNumber(value)) {
                    sender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create()
                            .add("value", value));
                    return;
                }
                long time = Long.parseLong(value);
                filter.time(time);
                return;
            }
            case "receiver": {
                filter.receiver(DKCoins.getInstance().getAccountManager().searchAccount(value));
                return;
            }
            case "currency": {
                filter.currency(DKCoins.getInstance().getCurrencyManager().searchCurrency(value));
                return;
            }
            case "reason": {
                filter.reason(value);
                return;
            }
            case "cause": {
                filter.cause(value);
                return;
            }
            case "page": {
                if(!GeneralUtil.isNaturalNumber(value)) {
                    sender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create()
                            .add("value", value));
                    return;
                }
                int page = Integer.parseInt(value);
                filter.page(page);
            }
            default: {
                sender.sendMessage(Messages.COMMAND_BANK_BANK_STATEMENT_FILTER_OPTION_NOT_FOUND, VariableSet.create()
                        .add("filterOption", key));
            }
        }
    }
}
