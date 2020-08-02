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
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.Command;
import net.pretronic.libraries.command.command.object.DefinedNotFindable;
import net.pretronic.libraries.command.command.object.MainObjectCommand;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.command.object.ObjectNotFindable;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.Arrays;

public class CurrencyCommand extends MainObjectCommand<Currency> implements DefinedNotFindable<Currency>, ObjectNotFindable {

    private final ObjectCommand<String> createCommand;
    private final Command listCommand;
    private final ObjectCommand<Currency> infoCommand;

    public CurrencyCommand(ObjectOwner owner) {
        super(owner, DKCoinsConfig.COMMAND_CURRENCY);
        this.createCommand = new CurrencyCreateCommand(owner);
        this.listCommand = new CurrencyListCommand(owner);
        this.infoCommand = new CurrencyInfoCommand(owner);
        registerCommand(infoCommand);
        registerCommand(new CurrencyDeleteCommand(owner));
        registerCommand(new CurrencyEditCommand(owner));
    }

    @Override
    public Currency getObject(CommandSender sender, String currency) {
        return DKCoins.getInstance().getCurrencyManager().getCurrency(currency);
    }

    @Override
    public void commandNotFound(CommandSender commandSender, Currency currency, String command, String[] args) {
        if(currency != null) {
            if(command == null) {
                infoCommand.execute(commandSender, currency, args);
            } else {
                commandSender.sendMessage(Messages.COMMAND_CURRENCY_HELP);
            }
        } else {
            listCommand.execute(commandSender, args);
        }
    }

    @Override
    public void objectNotFound(CommandSender commandSender, String command, String[] args) {
        if(command.equalsIgnoreCase("list")) {
            listCommand.execute(commandSender, args);
        } else if(command.equalsIgnoreCase("help")) {
            commandSender.sendMessage(Messages.COMMAND_CURRENCY_HELP);
        } else if(args.length > 0) {
            if(args[0].equalsIgnoreCase("create")) {
                createCommand.execute(commandSender, command, args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : args);
            }
        } else {
            commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create()
                    .add("name", command));
        }
    }
}
