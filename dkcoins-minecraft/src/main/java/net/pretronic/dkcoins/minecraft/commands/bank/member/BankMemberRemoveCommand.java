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

package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.player.MinecraftPlayer;

public class BankMemberRemoveCommand extends ObjectCommand<AccountMember> {

    public BankMemberRemoveCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("remove"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] strings) {
        if(commandSender instanceof MinecraftPlayer && member.equals(member.getAccount().getMember(CommandUtil.getUserByCommandSender(commandSender)))) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_YOURSELF);
            return;
        }
        member.getAccount().removeMember(member, CommandUtil.getAccountMemberByCommandSender(commandSender, member.getAccount()));
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_REMOVE, VariableSet.create()
                .addDescribed("user", member.getUser()));
    }
}
