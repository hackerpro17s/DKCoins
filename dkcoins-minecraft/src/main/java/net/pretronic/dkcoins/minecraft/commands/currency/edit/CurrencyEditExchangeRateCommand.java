package net.pretronic.dkcoins.minecraft.commands.currency.edit;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.Collection;
import java.util.Collections;

public class CurrencyEditExchangeRateCommand extends ObjectCommand<Currency> implements Completable {

    public CurrencyEditExchangeRateCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("exchangeRate", "exchange"));
    }

    @Override
    public void execute(CommandSender sender, Currency currency, String[] args) {
        if(args.length != 2) {
            sender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_HELP);
            return;
        }
        String targetCurrency0 = args[0];
        Currency targetCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(targetCurrency0);
        if(targetCurrency == null) {
            sender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create()
                    .add("name", targetCurrency0));
            return;
        }
        String argument = args[1];
        if(argument.equalsIgnoreCase("disable")) {
            currency.setExchangeRate(targetCurrency, -1);
            sender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DISABLE_EXCHANGE_RATE, VariableSet.create()
                    .addDescribed("currency", currency)
                    .addDescribed("targetCurrency", targetCurrency));
            return;
        }

        if(!GeneralUtil.isNumber(argument)) {
            sender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create()
                    .add("value", argument));
            return;
        }

        double amount = Double.parseDouble(argument);
        if(amount <= 0) {
            sender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_EXCHANGE_RATE_AMOUNT_NOT_VALID, VariableSet.create()
                    .add("value", DKCoinsConfig.formatCurrencyAmount(amount)));
            return;
        }

        CurrencyExchangeRate exchangeRate = currency.setExchangeRate(targetCurrency, amount);
        sender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_EXCHANGE_RATE, VariableSet.create()
                .addDescribed("exchangeRate", exchangeRate));
    }

    @Override
    public Collection<String> complete(CommandSender commandSender, String[] args) {
        if(args.length == 0){
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName);
        }else if(args.length == 1){
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName
                    ,currency -> currency.getName().toLowerCase().startsWith(args[0].toLowerCase()));
        }
        return Collections.emptyList();
    }
}
