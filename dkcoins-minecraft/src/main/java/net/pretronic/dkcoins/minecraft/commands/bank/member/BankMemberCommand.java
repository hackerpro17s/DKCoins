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
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.commands.bank.AbstractBankLimitCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Pair;
import net.pretronic.libraries.utility.map.Triple;

import java.util.Arrays;

public class BankMemberCommand extends ObjectCommand<BankAccount> {

    private final ObjectCommand<BankAccount> listCommand;
    private final ObjectCommand<AccountMember> infoCommand;
    private final ObjectCommand<AccountMember> roleCommand;
    private final ObjectCommand<AccountMember> removeCommand;
    private final ObjectCommand<Triple<BankAccount, AccountMemberRole, AccountMember>> limitCommand;
    private final ObjectCommand<Pair<BankAccount, String>> addCommand;

    public BankMemberCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("member"));
        this.listCommand = new BankMemberListCommand(owner);
        this.infoCommand = new BankMemberInfoCommand(owner);
        this.roleCommand = new BankMemberRoleCommand(owner);
        this.removeCommand = new BankMemberRemoveCommand(owner);
        this.limitCommand = new BankMemberLimitCommand(owner);
        this.addCommand = new BankMemberAddCommand(owner);
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(CommandUtil.hasAccessAndSendMessage(commandSender, account, AccessRight.MEMBER_MANAGEMENT)) {
            if(account.getType().getName().equalsIgnoreCase("User")) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_USER_NOT_POSSIBLE);
                return;
            }
            if(args.length < 1 || args[0].equalsIgnoreCase("list")) {
                this.listCommand.execute(commandSender, account, args);
                return;
            }
            String name = args[0];

            if(args.length < 2) {
                AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                if(member != null) {
                    this.infoCommand.execute(commandSender, member, args);
                }
                return;
            }
            switch (args[1].toLowerCase()) {
                case "help": {
                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_HELP);
                    break;
                }
                case "info": {
                    AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                    if(member != null) {
                        this.infoCommand.execute(commandSender, member, args);
                    }
                    break;
                }
                case "role": {
                    AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                    if(member != null) {
                        this.roleCommand.execute(commandSender, member, Arrays.copyOfRange(args, 2, args.length));
                    }
                    break;
                }
                case "remove": {
                    AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                    if(member != null) {
                        this.removeCommand.execute(commandSender, member, args);
                    }
                    break;
                }
                case "limit": {
                    AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                    if(member != null) {
                        this.limitCommand.execute(commandSender, new Triple<>(member.getAccount(), member.getRole(), member), Arrays.copyOfRange(args, 2, args.length));
                    }
                    break;
                }
                case "add": {
                    this.addCommand.execute(commandSender, new Pair<>(account, name), Arrays.copyOfRange(args, 2, args.length));
                    break;
                }
                default: {
                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_HELP);
                }
            }
        }
    }
}
