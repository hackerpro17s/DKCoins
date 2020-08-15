/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 05.08.20, 14:49
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

package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Triple;
import org.mcnative.common.player.MinecraftPlayer;
import org.mcnative.common.text.components.MessageKeyComponent;

import java.util.Collection;

public abstract class AbstractBankLimitCommand<T> extends ObjectCommand<Triple<BankAccount, AccountMemberRole, T>> {

    private final MessageKeyComponent helpMessage;

    public AbstractBankLimitCommand(ObjectOwner owner, MessageKeyComponent helpMessage) {
        super(owner, CommandConfiguration.name("limit"));
        this.helpMessage = helpMessage;
    }

    @Override
    public void execute(CommandSender commandSender, Triple<BankAccount, AccountMemberRole, T> target, String[] args) {
        if(CommandUtil.hasAccessAndSendMessage(commandSender, target.getFirst(), AccessRight.LIMIT_MANAGEMENT)) {
            if(args.length == 0) {
                listLimitations(commandSender, target);
                return;
            } else {
                switch (args[0].toLowerCase()) {
                    case "list": {
                        listLimitations(commandSender, target);
                        return;
                    }
                    case "set":
                    case "remove": {
                        if(!(commandSender instanceof ConsoleCommandSender || (commandSender instanceof MinecraftPlayer
                                && target.getFirst().getMember(DKCoins.getInstance().getUserManager()
                                .getUser(((MinecraftPlayer)commandSender).getUniqueId())).getRole().isHigher(target.getSecond())))) {
                            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_LOWER,
                                    VariableSet.create().addDescribed("targetRole", target.getSecond()));
                            return;
                        }
                        if(args.length == 5) {
                            AccountLimitation.Interval interval = AccountLimitation.Interval.parse(args[1]);
                            if(interval == null) {
                                commandSender.sendMessage(Messages.ERROR_ACCOUNT_LIMITATION_INTERVAL_NOT_VALID, VariableSet.create().add("value", args[1]));
                                return;
                            }
                            String amount0 = args[2];
                            if(!GeneralUtil.isNumber(amount0)) {
                                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", amount0));
                                return;
                            }
                            double amount = Double.parseDouble(amount0);

                            AccountLimitation.CalculationType calculationType = AccountLimitation.CalculationType.parse(args[3]);
                            if(calculationType == null) {
                                commandSender.sendMessage(Messages.ERROR_ACCOUNT_LIMITATION_CALCULATION_TYPE_NOT_VALID, VariableSet.create().add("value", args[3]));
                                return;
                            }
                            Currency currency = DKCoins.getInstance().getCurrencyManager().getCurrency(args[4]);
                            if(currency == null) {
                                commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create().add("name", args[4]));
                                return;
                            }

                            if(args[0].equalsIgnoreCase("set")) {
                                AccountLimitation limitation = addLimitation(target, currency, calculationType, amount, interval);
                                commandSender.sendMessage(Messages.COMMAND_BANK_LIMIT_SET, VariableSet.create()
                                        .addDescribed("limitation", limitation));
                            } else {
                                AccountLimitation limitation = getLimitation(target, currency, calculationType, amount, interval);
                                if(target.getFirst().removeLimitation(limitation)) {
                                    commandSender.sendMessage(Messages.COMMAND_BANK_LIMIT_REMOVE, VariableSet.create()
                                            .addDescribed("limitation", limitation));
                                } else {
                                    commandSender.sendMessage(Messages.COMMAND_BANK_LIMIT_REMOVE_FAILURE);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        commandSender.sendMessage(this.helpMessage);
    }

    protected abstract Collection<AccountLimitation> getLimitations(Triple<BankAccount, AccountMemberRole, T> target);

    protected abstract AccountLimitation getLimitation(Triple<BankAccount, AccountMemberRole, T> target, Currency currency, AccountLimitation.CalculationType calculationType,
                                                       double amount, AccountLimitation.Interval interval);

    protected abstract AccountLimitation addLimitation(Triple<BankAccount, AccountMemberRole, T> target, Currency currency, AccountLimitation.CalculationType calculationType,
                                                       double amount, AccountLimitation.Interval interval);

    private void listLimitations(CommandSender commandSender, Triple<BankAccount, AccountMemberRole, T> target) {
        Collection<AccountLimitation> limitations = getLimitations(target);
        if(limitations.isEmpty()) {
            commandSender.sendMessage(Messages.COMMAND_BANK_INFO_NO_LIMITATION);
        } else {
            commandSender.sendMessage(Messages.COMMAND_BANK_INFO_LIMITATION, VariableSet.create()
                    .addDescribed("limitations", limitations));
        }
    }
}
