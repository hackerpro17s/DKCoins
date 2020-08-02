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
import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.player.MinecraftPlayer;

public class BankMemberLimitCommand extends ObjectCommand<AccountMember> {

    public BankMemberLimitCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("limit"));
    }

    @Override
    public void execute(CommandSender commandSender, AccountMember member, String[] args) {
        if(CommandUtil.hasAccessAndSendMessage(commandSender, member.getAccount(), AccessRight.LIMIT_MANAGEMENT)) {
            if(args.length == 0) {
                listLimitations(commandSender, member);
                return;
            } else {
                switch (args[0].toLowerCase()) {
                    case "list": {
                        listLimitations(commandSender, member);
                        return;
                    }
                    case "set":
                    case "remove": {
                        if(!(commandSender instanceof ConsoleCommandSender || (commandSender instanceof MinecraftPlayer
                                && member.getAccount().getMember(DKCoins.getInstance().getUserManager()
                                .getUser(((MinecraftPlayer)commandSender).getUniqueId())).getRole().isHigher(member.getRole())))) {
                            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_LOWER,
                                    VariableSet.create().addDescribed("targetRole", member.getRole()));
                            return;
                        }
                        if(args.length == 3) {
                            String interval0 = args[1];
                            if(!GeneralUtil.isNaturalNumber(interval0)) {
                                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", interval0));
                                return;
                            }
                            String amount0 = args[2];
                            if(!GeneralUtil.isNumber(amount0)) {
                                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", amount0));
                                return;
                            }
                            long interval = Long.parseLong(interval0);
                            double amount = Double.parseDouble(amount0);

                            if(args[0].equalsIgnoreCase("set")) {
                                AccountLimitation limitation = member.addLimitation(DKCoinsConfig.CURRENCY_DEFAULT, amount, interval);
                                commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIMIT_SET, VariableSet.create()
                                        .addDescribed("limitation", limitation)
                                        .addDescribed("member", member));
                            } else {
                                AccountLimitation limitation = member.getLimitation(DKCoinsConfig.CURRENCY_DEFAULT, amount, interval);
                                if(member.removeLimitation(limitation)) {
                                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIMIT_REMOVE, VariableSet.create()
                                            .addDescribed("limitation", limitation)
                                            .addDescribed("member", member));
                                } else {
                                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIMIT_REMOVE_FAILURE);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_LIMIT_HELP);
    }

    private void listLimitations(CommandSender commandSender, AccountMember member) {
        if(member.getLimitations().isEmpty()) {
            commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_NO_LIMITATION, VariableSet.create()
                    .addDescribed("member", member));
        } else {
            commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_INFO_LIMITATION, VariableSet.create()
                    .addDescribed("limitations", member.getLimitations()));
        }
    }
}
