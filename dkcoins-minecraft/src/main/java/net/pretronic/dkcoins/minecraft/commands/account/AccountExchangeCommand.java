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

import net.prematic.libraries.command.command.ObjectCommand;
import net.prematic.libraries.command.command.configuration.CommandConfiguration;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.utility.GeneralUtil;
import net.prematic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;
import org.mcnative.common.player.OnlineMinecraftPlayer;

import java.util.ArrayList;

public class AccountExchangeCommand extends ObjectCommand<BankAccount> {

    public AccountExchangeCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("exchange").create());
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {

            return;
        }
        if(args.length < 3) {

            return;
        }
        String sourceCurrency0 = args[0];
        String destinationCurrency0 = args[1];
        String amount0 = args[2];

        Currency sourceCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(sourceCurrency0);
        if(sourceCurrency == null) {

            return;
        }
        Currency destinationCurrency = DKCoins.getInstance().getCurrencyManager().searchCurrency(destinationCurrency0);
        if(destinationCurrency == null) {

            return;
        }
        if(!GeneralUtil.isNumber(amount0)) {

            return;
        }
        double amount = Double.parseDouble(amount0);

        OnlineMinecraftPlayer player = (OnlineMinecraftPlayer)commandSender;
        //AccountMember member = account.getMember(DKCoins.getInstance().getUserManager().getUser(player.getId()));
        AccountMember member = null;
        StringBuilder reasonBuilder = new StringBuilder();
        if(args.length > 3) {
            for (int i = 3; i < args.length; i++) {
                reasonBuilder.append(args[i]);
            }
        } else {
            reasonBuilder = new StringBuilder("none");
        }

        //@Todo property
        boolean success = account.exchangeAccountCredit(member, sourceCurrency, destinationCurrency, amount,
                reasonBuilder.toString(), new ArrayList<>());
        if(success) {

        } else {

        }
    }
}
