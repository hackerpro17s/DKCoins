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

import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.NoPermissionAble;
import net.pretronic.libraries.command.NotFindable;
import net.pretronic.libraries.command.command.MainCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class DKCoinsCommand extends MainCommand implements NotFindable, NoPermissionAble {

    private final DKCoinsInfoCommand infoCommand;

    public DKCoinsCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("dkcoins").permission(DKCoinsConfig.PERMISSIONS_ADMIN).create());
        infoCommand = new DKCoinsInfoCommand(owner);
        registerCommand(new DKCoinsMigrationCommand(owner));
        registerCommand(new DKCoinsBankAdminCommand(owner));
        registerCommand(infoCommand);
    }

    @Override
    public void commandNotFound(CommandSender sender, String s, String[] strings) {
        sender.sendMessage(Messages.COMMAND_DKCOINS_HELP);
    }

    @Override
    public void noPermission(CommandSender sender, String s, String s1, String[] arguments) {
        infoCommand.execute(sender,arguments);
    }
}
