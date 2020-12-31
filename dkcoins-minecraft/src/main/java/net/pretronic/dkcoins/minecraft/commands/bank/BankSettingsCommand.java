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

package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Convert;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;

public class BankSettingsCommand extends ObjectCommand<BankAccount> {

    public BankSettingsCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("settings", "edit"));
    }

    //settings <setting> <value>
    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(commandSender.equals(McNative.getInstance().getConsoleSender())) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(args.length != 2) {
            commandSender.sendMessage(Messages.COMMAND_BANK_SETTINGS_HELP);
            return;
        }

        String setting = args[0];
        switch (setting.toLowerCase()) {
            case "receivenotifications": {
                AccountMember member = CommandUtil.getAccountMemberByCommandSender(commandSender, account);
                boolean receiveNotifications;
                try {
                    receiveNotifications = Convert.toBoolean(args[1]);
                } catch (IllegalArgumentException ignored) {
                    commandSender.sendMessage(Messages.ERROR_NOT_BOOLEAN, VariableSet.create()
                            .add("value", args[1]));
                    return;
                }
                member.setReceiveNotifications(receiveNotifications);
                if(member.receiveNotifications()) {
                    commandSender.sendMessage(Messages.COMMAND_BANK_SETTINGS_RECEIVE_NOTIFICATIONS_ON);
                } else {
                    commandSender.sendMessage(Messages.COMMAND_BANK_SETTINGS_RECEIVE_NOTIFICATIONS_OFF);
                }
                return;
            }
            default: {
                commandSender.sendMessage(Messages.COMMAND_BANK_SETTINGS_NOT_VALID, VariableSet.create()
                        .add("value", args[1]));
            }
        }
    }
}
