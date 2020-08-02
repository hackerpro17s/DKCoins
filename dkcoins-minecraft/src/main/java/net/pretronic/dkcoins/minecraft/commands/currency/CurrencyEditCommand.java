/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 02.08.20, 20:44
 * @web %web%
 *
 * The DKCoins Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.pretronic.dkcoins.minecraft.commands.currency;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.player.OnlineMinecraftPlayer;

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
                        .add("oldName", oldName)
                        .addDescribed("currency", currency));
                return;
            } else if(action.equalsIgnoreCase("symbol")) {
                final String oldSymbol = currency.getSymbol();
                String symbol = args[1];
                currency.setSymbol(symbol);
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_SYMBOL, VariableSet.create()
                        .add("oldSymbol", oldSymbol)
                        .addDescribed("currency", currency));
                return;
            }
        } else if(args.length == 3 && (args[0].equalsIgnoreCase("exchangeRate")
                || args[0].equalsIgnoreCase("exchange")) ) {
            String targetCurrency0 = args[1];
            Currency targetCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(targetCurrency0);
            if(targetCurrency == null) {
                commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create()
                        .add("name", targetCurrency0));
                return;
            }
            String argument = args[2];
            if(argument.equalsIgnoreCase("disable")) {
                currency.setExchangeRate(targetCurrency, -1);
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DISABLE_EXCHANGE_RATE, VariableSet.create()
                        .addDescribed("currency", currency)
                        .addDescribed("targetCurrency", targetCurrency));
                return;
            }
            if(!GeneralUtil.isNumber(argument)) {
                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create()
                        .add("value", argument));
                return;
            }
            double amount = Double.parseDouble(argument);
            if(amount <= 0) {
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_EXCHANGE_RATE_AMOUNT_NOT_VALID, VariableSet.create()
                        .add("value", DKCoinsConfig.formatCurrencyAmount(amount)));
                return;
            }
            CurrencyExchangeRate exchangeRate = currency.setExchangeRate(targetCurrency, amount);
            commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_DONE_EXCHANGE_RATE, VariableSet.create()
                    .addDescribed("exchangeRate", exchangeRate));
            return;
        }
        commandSender.sendMessage(Messages.COMMAND_CURRENCY_EDIT_HELP);

    }
}
