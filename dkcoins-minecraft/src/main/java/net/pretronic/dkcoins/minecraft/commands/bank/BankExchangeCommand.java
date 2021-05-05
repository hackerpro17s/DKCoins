/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 04.02.20, 15:50
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands.bank;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transferresult.TransferResult;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class BankExchangeCommand extends ObjectCommand<BankAccount> implements Completable {

    // bank <name> exchange <sourceCurrency> <targetCurrency> <amount>
    public BankExchangeCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("exchange"));
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {
            commandSender.sendMessage(Messages.ERROR_NOT_FROM_CONSOLE);
            return;
        }
        if(args.length < 3) {
            commandSender.sendMessage(Messages.COMMAND_BANK_EXCHANGE_HELP);
            return;
        }
        if(CommandUtil.hasAccountAccess(commandSender, account, AccessRight.EXCHANGE)) {
            String sourceCurrency0 = args[0];
            String destinationCurrency0 = args[1];
            String amount0 = args[2];

            Currency sourceCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(sourceCurrency0);
            if(sourceCurrency == null) {
                commandSender.sendMessage(Messages.COMMAND_BANK_EXCHANGE_HELP);
                return;
            }
            Currency targetCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(destinationCurrency0);
            if(targetCurrency == null) {
                commandSender.sendMessage(Messages.COMMAND_BANK_EXCHANGE_HELP);
                return;
            }
            if(!GeneralUtil.isNumber(amount0)) {
                commandSender.sendMessage(Messages.COMMAND_BANK_EXCHANGE_HELP);
                return;
            }
            double amount = Double.parseDouble(amount0);

            OnlineMinecraftPlayer player = (OnlineMinecraftPlayer) commandSender;
            AccountMember member = account.getMember(player.getAs(DKCoinsUser.class));

            TransferResult result = account.exchangeAccountCredit(member, sourceCurrency, targetCurrency, amount,
                    CommandUtil.buildReason(args, 3), DKCoins.getInstance().getTransactionPropertyBuilder().build(member));
            if(result.isSuccess()) {
                player.sendMessage(Messages.COMMAND_BANK_EXCHANGE_SUCCESS, VariableSet.create()
                        .addDescribed("sourceCurrency", sourceCurrency)
                        .addDescribed("targetCurrency", targetCurrency)
                        .add("sourceAmount", DKCoinsConfig.formatCurrencyAmount(amount))
                        .add("targetAmount", sourceCurrency.exchange(amount, targetCurrency)));
            } else {
                CommandUtil.handleTransferFailCauses(result, commandSender);
            }
        } else {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_NOT_ENOUGH_ACCESS_RIGHTS);
        }
    }

    @Override
    public Collection<String> complete(CommandSender sender, String[] args) {
        if(args.length == 0){
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName);
        }else  if(args.length == 1){
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName
                    ,currency -> currency.getName().toLowerCase().startsWith(args[0].toLowerCase()));
        }else  if(args.length == 2){
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName
                    ,currency -> currency.getName().toLowerCase().startsWith(args[1].toLowerCase())
                            && !currency.getName().equalsIgnoreCase(args[0]));
        }
        return Collections.emptyList();
    }
}
