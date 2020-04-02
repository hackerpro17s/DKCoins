package net.pretronic.dkcoins.minecraft.commands.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

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
        /*sender.sendMessage(Messages.TOP_HEADER, VariableSet.create().add("amount", limit));
        int rank = 1;
        for (BankAccount account : DKCoins.getInstance().getAccountManager().getTopAccounts(currency, new AccountType[0], limit)) {
            sender.sendMessage(Messages.TOP_ENTRY, VariableSet.create()
                    .add("rank", rank++)
                    .add("name", account.getName())
                    .add("amount", account.getCredit(currency).getAccount()));
        }*/
    }
}
