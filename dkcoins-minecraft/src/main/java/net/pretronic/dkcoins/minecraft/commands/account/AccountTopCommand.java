package net.pretronic.dkcoins.minecraft.commands.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.List;

public class AccountTopCommand extends ObjectCommand<Currency> {

    public AccountTopCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("top"));
    }

    @Override
    public void execute(CommandSender sender, Currency currency, String[] args) {
        int limit = DKCoinsConfig.TOP_LIMIT_DEFAULT;
        if(args.length > 0) {
            String limitValue = args[0];
            if(!GeneralUtil.isNaturalNumber(limitValue)) {
                sender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", limitValue));
                return;
            }
            limit = Integer.parseInt(limitValue);
            if(limit > DKCoinsConfig.TOP_LIMIT_MAX) {
                sender.sendMessage(Messages.TOP_REACH_LIMIT);
                return;
            }
        }
        List<BankAccount> accounts = DKCoins.getInstance().getAccountManager().getTopAccounts(currency, new AccountType[0], limit);
        List<AccountCredit> credits = Iterators.map(accounts, account -> account.getCredit(currency));
        sender.sendMessage(Messages.TOP, new ReflectVariableSet()
                .add("amount", limit)
                .add("credits", credits));
    }
}
