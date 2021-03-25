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

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.commands.bank.limit.BankLimitCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.DefinedNotFindable;
import net.pretronic.libraries.command.command.object.multiple.MultipleMainObjectCommand;
import net.pretronic.libraries.command.command.object.multiple.MultipleObjectNotFindable;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Pair;

import java.util.Arrays;

public class BankMemberCommand extends MultipleMainObjectCommand<BankAccount, AccountMember> implements DefinedNotFindable<AccountMember>, MultipleObjectNotFindable<BankAccount> {

    private final BankMemberListCommand listCommand;
    private final BankMemberInfoCommand infoCommand;
    private final BankMemberAddCommand addCommand;

    public BankMemberCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("member"));
        this.listCommand = new BankMemberListCommand(owner);
        this.infoCommand = new BankMemberInfoCommand(owner);
        this.addCommand = new BankMemberAddCommand(owner);

        registerCommand(new BankMemberRoleCommand(owner));
        registerCommand(new BankMemberRemoveCommand(owner));
        registerCommand(new BankLimitCommand(owner, Messages.COMMAND_BANK_MEMBER_LIMIT_HELP));
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        System.out.println("Member access check");
        if(account.getType().getName().equalsIgnoreCase("User")) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_USER_NOT_POSSIBLE);
            return;
        }

        if(!CommandUtil.hasAccountAccess(commandSender, account, AccessRight.MEMBER_MANAGEMENT)) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_NOT_ENOUGH_ACCESS_RIGHTS);
            return;
        }
        super.execute(commandSender, account, args);
    }

    @Override
    public AccountMember getObject(CommandSender commandSender, BankAccount account, String name) {
        if(name.equalsIgnoreCase("list")) return null;
        return CommandUtil.parseAccountMember(account, name);
    }

    @Override
    public void commandNotFound(CommandSender commandSender, AccountMember member, String command, String[] args) {
        if(member != null && command == null) {
            infoCommand.execute(commandSender, member, args);
        } else {
            commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_HELP);
        }
    }

    @Override
    public void objectNotFound(CommandSender commandSender, BankAccount account, String command, String[] args) {
        if(command.equalsIgnoreCase("list") || command.equalsIgnoreCase("l")) {
            this.listCommand.execute(commandSender, account, args);
        } else if(args.length > 0 && (args[0].equalsIgnoreCase("add"))) {
            this.addCommand.execute(commandSender, new Pair<>(account, command), Arrays.copyOfRange(args, 1, args.length));
        } else {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_NOT_EXISTS, VariableSet.create().add("name", command));
        }
    }
}
