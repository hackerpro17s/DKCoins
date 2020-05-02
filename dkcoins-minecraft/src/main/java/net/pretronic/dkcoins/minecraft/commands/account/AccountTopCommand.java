package net.pretronic.dkcoins.minecraft.commands.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.RankedAccountCredit;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.describer.DescribedHashVariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
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
        List<RankedAccountCredit> ranks = DKCoins.getInstance().getAccountManager()
                .getTopAccountCredits(currency, new AccountType[0], DKCoinsConfig.TOP_LIMIT_ENTRIES_PER_PAGE, page);
        if(ranks.isEmpty()) {
            sender.sendMessage(Messages.TOP_PAGE_NO_ENTRIES);
            return;
        }
        int start = DKCoinsConfig.TOP_LIMIT_ENTRIES_PER_PAGE * (page - 1) + 1;
        int end = page * DKCoinsConfig.TOP_LIMIT_ENTRIES_PER_PAGE;
        sender.sendMessage(Messages.TOP, VariableSet.create()
                .add("start", start)
                .add("end", end)
                .addDescribed("ranks", ranks)
                .add("page", page));
    }
}
