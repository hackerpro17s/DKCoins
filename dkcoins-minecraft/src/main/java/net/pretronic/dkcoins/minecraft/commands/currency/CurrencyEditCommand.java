package net.pretronic.dkcoins.minecraft.commands.currency;

import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.command.object.ObjectCommand;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.message.bml.variable.VariableSet;
import net.prematic.libraries.utility.GeneralUtil;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
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
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_NAME, VariableSet.create()
                        .add("oldName", oldName).add("name", name));
                return;
            } else if(action.equalsIgnoreCase("symbol")) {
                String symbol = args[1];
                currency.setSymbol(symbol);
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_SYMBOL, VariableSet.create()
                        .add("name", currency.getName()).add("symbol", symbol));
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
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DISABLE_EXCHANGE_RATE, VariableSet.create()
                        .add("source", currency.getName()).add("target", targetCurrency.getName()));
                return;
            }
            if(!GeneralUtil.isNumber(argument)) {
                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", argument));
                return;
            }
            double amount = Double.parseDouble(argument);
            if(amount <= 0) {
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_EXCHANGE_RATE_AMOUNT_NOT_VALID, VariableSet.create()
                        .add("amount", amount));
                return;
            }
            currency.setExchangeRate(targetCurrency, amount);
            commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_EXCHANGE_RATE, VariableSet.create()
                    .add("name", currency.getName()).add("target", targetCurrency.getName()).add("amount", amount));
            return;
        }
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_HELP);

    }
}
