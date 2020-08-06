/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 05.08.20, 14:38
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

package net.pretronic.dkcoins.minecraft.commands.bank.role;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Triple;

import java.util.Arrays;

public class BankRoleCommand extends ObjectCommand<BankAccount> {

    private final ObjectCommand<Triple<BankAccount, AccountMemberRole, AccountMemberRole>> limitCommand;

    public BankRoleCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("role"));
        this.limitCommand = new BankRoleLimitCommand(owner);
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(CommandUtil.hasAccessAndSendMessage(commandSender, account, AccessRight.ROLE_MANAGEMENT)) {
            if(account.getType().getName().equalsIgnoreCase("User")) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_USER_NOT_POSSIBLE);
                return;
            }
            if(args.length < 2) {
                commandSender.sendMessage(Messages.COMMAND_BANK_ROLE_HELP);
                return;
            }
            String roleName = args[0];
            AccountMemberRole role = AccountMemberRole.byName(roleName);
            if(role == null) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_NOT_EXISTS, VariableSet.create().add("name", roleName));
                return;
            }
            if(args[1].equalsIgnoreCase("limit")) {
                this.limitCommand.execute(commandSender, new Triple<>(account, role, role), Arrays.copyOfRange(args, 2, args.length));
            } else {
                commandSender.sendMessage(Messages.COMMAND_BANK_ROLE_HELP);
            }
        }
    }
}
