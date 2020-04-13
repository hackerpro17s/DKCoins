/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 04.02.20, 15:50
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands.account;

import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.reflect.ReflectVariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.TransferResult;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class AccountExchangeCommand extends ObjectCommand<BankAccount> {

    // bank <name> exchange <sourceCurrency> <targetCurrency> <amount>
    public AccountExchangeCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("exchange"));
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(args.length < 3) {
            commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_HELP);
            return;
        }
        if(CommandUtil.hasAccessAndSendMessage(commandSender, account, AccessRight.WITHDRAW)) {
            String sourceCurrency0 = args[0];
            String destinationCurrency0 = args[1];
            String amount0 = args[2];

            Currency sourceCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(sourceCurrency0);
            if(sourceCurrency == null) {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_HELP);
                return;
            }
            Currency targetCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(destinationCurrency0);
            if(targetCurrency == null) {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_HELP);
                return;
            }
            if(!GeneralUtil.isNumber(amount0)) {
                commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_HELP);
                return;
            }
            double amount = Double.parseDouble(amount0);

            OnlineMinecraftPlayer player = (OnlineMinecraftPlayer) commandSender;
            AccountMember member = account.getMember(DKCoins.getInstance().getUserManager().getUser(player.getUniqueId()));

            TransferResult result = account.exchangeAccountCredit(member, sourceCurrency, targetCurrency, amount,
                    CommandUtil.buildReason(args, 3), DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
            if(result.isSuccess()) {
                player.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_SUCCESS, new ReflectVariableSet()
                        .add("sourceCurrency", sourceCurrency)
                        .add("targetCurrency", targetCurrency)
                        .add("sourceAmount", DKCoinsConfig.formatCurrencyAmount(amount))
                        .add("targetAmount", sourceCurrency.exchange(amount, targetCurrency)));
            } else {
                switch (result.getFailCause()) {
                    case LIMIT: {
                        commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_FAILURE_LIMIT);
                        break;
                    }
                    case NOT_ENOUGH_AMOUNT: {
                        commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_FAILURE_NOT_ENOUGH_AMOUNT);
                        break;
                    }
                    case NOT_ENOUGH_ACCESS_RIGHTS: {
                        commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_FAILURE_NOT_ENOUGH_ACCESS_RIGHTS);
                        break;
                    }
                    case MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT: {
                        commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_FAILURE_MASTER_ACCOUNT_NOT_ENOUGH_AMOUNT);
                        break;
                    }
                    case TRANSFER_DISABLED: {
                        commandSender.sendMessage(Messages.COMMAND_ACCOUNT_EXCHANGE_FAILURE_DISABLED, VariableSet.create()
                                .add("sourceCurrency", sourceCurrency).add("targetCurrency", targetCurrency));
                        break;
                    }
                }
            }
        }
    }
}
