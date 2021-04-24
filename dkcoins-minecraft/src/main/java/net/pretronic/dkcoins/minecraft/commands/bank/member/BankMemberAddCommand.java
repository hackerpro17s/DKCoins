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
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.account.member.DefaultAccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Pair;
import org.mcnative.runtime.api.McNative;

public class BankMemberAddCommand extends ObjectCommand<Pair<BankAccount, String>> {

    public BankMemberAddCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("add"));
    }

    @Override
    public void execute(CommandSender commandSender, Pair<BankAccount, String> pair, String[] strings) {
        BankAccount account = pair.getKey();
        String userName = pair.getValue();

        DKCoinsUser user = McNative.getInstance().getPlayerManager().getPlayer(userName).getAs(DKCoinsUser.class);
        if(user == null) {
            commandSender.sendMessage(Messages.ERROR_USER_NOT_EXISTS, VariableSet.create().add("name", userName));
            return;
        }
        AccountMember member = account.getMember(user);
        if(member == null) {
            member = account.addMember(user, CommandUtil.getAccountMemberByCommandSender(commandSender, account),
                    account.getRole(DefaultAccountMemberRole.GUEST), true);
            commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_ADD, VariableSet.create()
                    .addDescribed("member", member));
        } else {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ALREADY, VariableSet.create()
                    .addDescribed("member", member));
        }
    }
}
