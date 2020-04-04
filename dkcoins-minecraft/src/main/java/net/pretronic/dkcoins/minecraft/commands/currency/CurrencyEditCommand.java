package net.pretronic.dkcoins.minecraft.commands.currency;

import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.minecraft.Messages;
import org.mcnative.common.player.OnlineMinecraftPlayer;

import java.util.Arrays;

public class CurrencyEditCommand extends ObjectCommand<Currency> {

    public CurrencyEditCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("edit"));
    }

    @Override
    public void execute(CommandSender commandSender, Currency currency, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(args.length == 2) {
            String action = args[0];
            if(action.equalsIgnoreCase("name")) {
                final String oldName = currency.getName();
                String name = args[1];
                currency.setName(name);
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_NAME, new ReflectVariableSet()
                        .add("oldName", oldName)
                        .add("currency", currency));
                return;
            } else if(action.equalsIgnoreCase("symbol")) {
                final String oldSymbol = currency.getSymbol();
                String symbol = args[1];
                currency.setSymbol(symbol);
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_SYMBOL, new ReflectVariableSet()
                        .add("oldSymbol", oldSymbol)
                        .add("currency", currency));
                return;
            }
        } else if(args.length == 3 && (args[0].equalsIgnoreCase("exchangeRate")
                || args[0].equalsIgnoreCase("exchange")) ) {
            String targetCurrency0 = args[1];
            Currency targetCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(targetCurrency0);
            if(targetCurrency == null) {
                commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create().add("name", targetCurrency0));
                return;
            }
            String argument = args[2];
            if(argument.equalsIgnoreCase("disable")) {
                currency.setExchangeRate(targetCurrency, -1);
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DISABLE_EXCHANGE_RATE, new ReflectVariableSet()
                        .add("currency", currency)
                        .add("targetCurrency", targetCurrency));
                return;
            }
            if(!GeneralUtil.isNumber(argument)) {
                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", argument));
                return;
            }
            double amount = Double.parseDouble(argument);
            if(amount <= 0) {
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_EXCHANGE_RATE_AMOUNT_NOT_VALID, VariableSet.create()
                        .add("value", DKCoinsConfig.formatCurrencyAmount(amount)));
                return;
            }
            CurrencyExchangeRate exchangeRate = currency.setExchangeRate(targetCurrency, amount);
            commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_EXCHANGE_RATE, new ReflectVariableSet()
                    .add("exchangeRate", exchangeRate));
            return;
        }
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_HELP);

    }
}
