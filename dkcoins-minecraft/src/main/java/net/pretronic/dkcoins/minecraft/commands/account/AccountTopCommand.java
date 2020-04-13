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
import net.pretronic.libraries.message.bml.variable.describer.DescribedHashVariableSet;
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
        int page = 1;
        if(args.length > 0) {
            String page0 = args[0];
            if(!GeneralUtil.isNaturalNumber(page0)) {
                sender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", page0));
                return;
            }
            page = Integer.parseInt(page0);
        }
        List<BankAccount> accounts = DKCoins.getInstance().getAccountManager().getTopAccounts(currency, new AccountType[0], DKCoinsConfig.TOP_LIMIT_ENTRIES_PER_PAGE, page);
        if(accounts.isEmpty()) {
            sender.sendMessage(Messages.TOP_PAGE_NO_ENTRIES);
            return;
        }
        List<AccountCredit> credits = Iterators.map(accounts, account -> account.getCredit(currency));
        sender.sendMessage(Messages.TOP, new DescribedHashVariableSet()
                .add("amount", -1)
                .add("credits", credits)
                .add("page", page));
    }
}
