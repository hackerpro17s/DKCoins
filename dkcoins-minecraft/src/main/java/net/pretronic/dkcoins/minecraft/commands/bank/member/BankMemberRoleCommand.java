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
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.common.account.member.DefaultAccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.DefinedCompletable;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.command.object.ObjectCompletable;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.player.MinecraftPlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

public class BankMemberRoleCommand extends ObjectCommand<AccountMember> implements DefinedCompletable<AccountMember> {

    public BankMemberRoleCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("role"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] args) {
        if(CommandUtil.hasAccountAccess(commandSender, member.getAccount(), AccessRight.ROLE_MANAGEMENT)) {
            if(args.length == 0) {
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_ROLE_HELP);
                return;
            }
            String role0 = args[0];
            AccountMemberRole role = member.getAccount().getRole(role0);
            if(role == null) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_NOT_EXISTS, VariableSet.create().add("name", role0));
                return;
            }
            if(commandSender instanceof ConsoleCommandSender || (commandSender instanceof MinecraftPlayer
                    && !member.getAccount().getMember(DKCoins.getInstance().getUserManager()
                    .getUser(((MinecraftPlayer)commandSender).getUniqueId())).getRole().isHigher(member.getRole()))) {
                AccountMember self = CommandUtil.getAccountMemberByCommandSender(commandSender, member.getAccount());
                if(commandSender instanceof MinecraftPlayer && member.equals(self)) {
                    commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_YOURSELF);
                    return;
                }
                member.setRole(role);
                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_ROLE, VariableSet.create()
                        .addDescribed("member", member));
                if(role.equals(member.getAccount().getRole(DefaultAccountMemberRole.OWNER))) {
                    if(self != null) {
                        self.setRole(member.getAccount().getRole(DefaultAccountMemberRole.ADMIN));
                    }
                }
            } else {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_LOWER,
                        VariableSet.create().addDescribed("targetRole", member.getRole()));
            }
        }
    }

    @Override
    public Collection<String> complete(CommandSender sender, AccountMember member, String[] args) {
        if(args.length == 0){
            return Iterators.map(member.getAccount().getRoles()
                    ,AccountMemberRole::getName);
        }else if(args.length == 1){
            return Iterators.map(member.getAccount().getRoles()
                    ,AccountMemberRole::getName
                    ,member1 -> member1.getName().toLowerCase().startsWith(args[0].toLowerCase()));
        }
        return Collections.emptyList();
    }
}
