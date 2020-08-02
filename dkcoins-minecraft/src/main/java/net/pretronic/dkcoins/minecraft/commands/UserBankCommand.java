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

package net.pretronic.dkcoins.minecraft.commands;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.TransferResult;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.account.TransferCause;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.account.AccountTopCommand;
import net.pretronic.dkcoins.minecraft.config.CreditAlias;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;
import org.mcnative.service.entity.living.Player;
import org.mcnative.service.world.World;

import java.util.Arrays;

public class UserBankCommand extends BasicCommand {

    private final CreditAlias creditAlias;
    private final ObjectCommand<Currency> topCommand;

    public UserBankCommand(ObjectOwner owner, CommandConfiguration configuration, CreditAlias creditAlias) {
        super(owner, configuration);
        Validate.notNull(creditAlias);
        this.creditAlias = creditAlias;
        this.topCommand = new AccountTopCommand(owner);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof MinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(McNative.getInstance().getPlatform().isService() && commandSender instanceof Player) {
            Player player = (Player) commandSender;
            World world = player.getLocation().getWorld();
            if(world != null) {
                for (String disabledWorld : creditAlias.getDisabledWorlds()) {
                    if(disabledWorld.equalsIgnoreCase(world.getName())) {
                        player.sendMessage(Messages.COMMAND_USER_BANK_WORLD_DISABLED, VariableSet.create()
                                .add("world", world.getName()));
                        return;
                    }
                }
            }
        }

        DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(((MinecraftPlayer)commandSender).getUniqueId());
        if(args.length == 0) {
            AccountCredit credit = user.getDefaultAccount().getCredit(getCurrency());
            commandSender.sendMessage(Messages.COMMAND_USER_BANK_AMOUNT, VariableSet.create()
                    .addDescribed("credit", credit));
            return;
        }
        String action = args[0];
        if(action.equalsIgnoreCase("pay") || action.equalsIgnoreCase("transfer")) {
            if(args.length == 3) {
                AccountCredit credit = user.getDefaultAccount().getCredit(getCurrency());
                AccountMember member = user.getDefaultAccount().getMember(user);
                String receiver0 = args[1];

                String amount0 = args[2];
                if(!GeneralUtil.isNumber(amount0)) {
                    commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create()
                            .add("value", amount0));
                    return;
                }
                double amount = Double.parseDouble(amount0);


                if(DKCoinsConfig.isPaymentAllAlias(receiver0)) {
                    if(CommandUtil.canTransferAndSendMessage(commandSender, amount, true)) {
                        CommandUtil.loopThroughUserBanks(user.getDefaultAccount(), receiver ->
                                transfer(commandSender, member, credit, receiver, amount, args));
                    }
                    return;
                }

                BankAccount receiver = DKCoins.getInstance().getAccountManager().searchAccount(receiver0);
                if(receiver == null) {
                    commandSender.sendMessage(Messages.ERROR_ACCOUNT_NOT_EXISTS, VariableSet.create()
                            .add("name", receiver0));
                    return;
                }
                if(CommandUtil.canTransferAndSendMessage(commandSender, amount, false)) {
                    transfer(commandSender, member, credit, receiver, amount, args);
                }
            } else {
                commandSender.sendMessage(Messages.COMMAND_USER_BANK_HELP, VariableSet.create()
                        .add("currency", getCurrency().getName()));
            }
        } else if(action.equalsIgnoreCase("top")) {
            this.topCommand.execute(commandSender, getCurrency(), args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
        } else {
            DKCoinsUser target = DKCoins.getInstance().getUserManager().getUser(action);
            if(target == null) {
                commandSender.sendMessage(Messages.ERROR_USER_NOT_EXISTS, VariableSet.create()
                        .add("name", action));
                return;
            }
            if(!commandSender.hasPermission(creditAlias.getOtherPermission())) {
                commandSender.sendMessage(Messages.ERROR_NO_PERMISSION);
            }
            AccountCredit credit = target.getDefaultAccount().getCredit(getCurrency());
            commandSender.sendMessage(Messages.COMMAND_USER_BANK_AMOUNT_OTHER, VariableSet.create()
                    .addDescribed("other", target.getDefaultAccount().getMember(target))
                    .addDescribed("credit", credit));
        }
    }

    private Currency getCurrency() {
        Currency currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(this.creditAlias.getCurrency());
        Validate.notNull(currency);
        return currency;
    }

    private void transfer(CommandSender commandSender, AccountMember member, AccountCredit credit, BankAccount receiver, double amount, String[] args) {
        TransferResult result = credit.transfer(member, amount, receiver.getCredit(credit.getCurrency()),
                CommandUtil.buildReason(args, 3), TransferCause.TRANSFER,
                DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
        if(result.isSuccess()) {
            commandSender.sendMessage(Messages.COMMAND_ACCOUNT_TRANSFER_SUCCESS, VariableSet.create()
                    .addDescribed("transaction", result.getTransaction()));
        } else {
            CommandUtil.handleTransferFailCauses(result, commandSender);
        }
    }
}
