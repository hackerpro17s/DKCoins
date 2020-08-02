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

import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class BankMemberInfoCommand extends ObjectCommand<AccountMember> {

    public BankMemberInfoCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("info"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] strings) {
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO,
                VariableSet.create()
                        .add("member", member));
        if(CommandUtil.hasAccess(commandSender, member.getAccount(), AccessRight.LIMIT_MANAGEMENT)) {
            if(member.getLimitations().isEmpty()) {
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_NO_LIMITATION, VariableSet.create()
                        .addDescribed("member", member));
            } else {
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_LIMITATION, VariableSet.create()
                        .addDescribed("limitations", member.getLimitations())
                        .addDescribed("member", member));
            }
        }
    }
}
