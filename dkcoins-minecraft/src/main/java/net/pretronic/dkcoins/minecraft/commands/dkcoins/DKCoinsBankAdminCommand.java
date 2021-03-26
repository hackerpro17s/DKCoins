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

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.account.TransferCause;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class DKCoinsBankAdminCommand extends BasicCommand implements Completable {

    private final List<String> ACTIONS = Arrays.asList("set","add","remove");

    public DKCoinsBankAdminCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder()
                .name("admin").aliases("bankAdmin")
                .permission("dkcoins.admin").create());
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(args.length < 3) {
            commandSender.sendMessage(Messages.COMMAND_DKCOINS_HELP);
            return;
        }
        String bankAccount = args[0];
        String action = args[1];
        String amount0 = args[2];
        String currency0 = args.length == 4 ? args[3] : null;

        if(!GeneralUtil.isNumber(amount0)) {
            commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create()
                    .add("value", amount0));
            return;
        }
        double amount = Double.parseDouble(amount0);

        Currency currency;
        if(currency0 == null) {
            currency = DKCoinsConfig.CURRENCY_DEFAULT;
        } else {
            currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(currency0);
            if(currency == null) {
                commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create()
                        .add("name", args[2]));
                return;
            }
        }

        if(DKCoinsConfig.isPaymentAllAlias(bankAccount)) {
            CommandUtil.loopThroughUserBanks(null, receiver -> transfer(commandSender, receiver, currency, amount, action, args));
        } else {
            BankAccount receiver = DKCoins.getInstance().getAccountManager().searchAccount(bankAccount);
            if(receiver == null) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS, VariableSet.create()
                        .add("name", bankAccount));
                return;
            }
            transfer(commandSender, receiver, currency, amount, action, args);
        }
    }

    private void transfer(CommandSender commandSender, BankAccount account, Currency currency, double amount, String action, String[] args) {
        AccountMember member = null;
        Collection<AccountTransactionProperty> properties = new ArrayList<>();
        if(!commandSender.equals(McNative.getInstance().getConsoleSender())) {
            MinecraftPlayer player = (MinecraftPlayer) commandSender;
            DKCoinsUser user = player.getAs(DKCoinsUser.class);
            member = account.getMember(user);
            if(member != null) {
                properties = DKCoins.getInstance().getTransactionPropertyBuilder().build(member);
            }
        }

        if(action.equalsIgnoreCase("set")) {
            AccountTransaction transaction = account.getCredit(currency)
                    .setAmount(member, amount, CommandUtil.buildReason(args, 3), TransferCause.ADMIN, properties);
            commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_SET, VariableSet.create()
                    .addDescribed("transaction", transaction));
        } else if(action.equalsIgnoreCase("add")) {
            AccountTransaction transaction = account.getCredit(currency).addAmount(member, amount,
                    CommandUtil.buildReason(args, 3), TransferCause.ADMIN, properties);
            commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_ADD, VariableSet.create()
                    .addDescribed("transaction", transaction));
        } else if(action.equalsIgnoreCase("remove")) {
            AccountTransaction transaction = account.getCredit(currency).removeAmount(member, amount,
                    CommandUtil.buildReason(args, 3), TransferCause.ADMIN, properties);
            commandSender.sendMessage(Messages.COMMAND_BANK_ADMIN_REMOVE, VariableSet.create()
                    .addDescribed("transaction", transaction));
        }
    }

    @Override
    public Collection<String> complete(CommandSender commandSender, String[] args) {
        if(args.length == 0){
            return Iterators.map(McNative.getInstance().getLocal().getConnectedPlayers()
                    ,MinecraftPlayer::getName);
        }else if(args.length == 1){
            return Iterators.map(McNative.getInstance().getLocal().getConnectedPlayers()
                    ,MinecraftPlayer::getName
                    ,player -> player.getName().toLowerCase().startsWith(args[0].toLowerCase()));
        }else if(args.length == 2){
            return Iterators.filter(ACTIONS, server -> server.startsWith(args[0].toLowerCase()));
        }else if(args.length == 4){
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName
                    ,currency -> currency.getName().toLowerCase().startsWith(args[3].toLowerCase()));
        }
        return Collections.emptyList();
    }
}
