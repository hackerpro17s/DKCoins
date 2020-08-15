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

package net.pretronic.dkcoins.minecraft.commands.dkcoins;

import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.NotFindable;
import net.pretronic.libraries.command.command.MainCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class DKCoinsCommand extends MainCommand implements NotFindable {

    public DKCoinsCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("dkcoins"));
        registerCommand(new DKCoinsMigrateCommand(owner));
        registerCommand(new DKCoinsBankAdminCommand(owner));
    }

    @Override
    public void commandNotFound(CommandSender sender, String command, String[] args) {
        if(sender.hasPermission("dkcoins.admin")) {
            sender.sendMessage(Messages.COMMAND_DKCOINS_HELP);
        } else {
            sender.sendMessage(String.format("DKCoins v%s was programmed by Pretronic (https://pretronic.net)",
                    DKCoinsPlugin.getInstance().getDescription().getVersion().getName()));
        }
    }
}
