/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 04.02.20, 15:29
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
import net.pretronic.dkcoins.minecraft.account.TransferCause;
import org.mcnative.common.player.OnlineMinecraftPlayer;

import java.util.ArrayList;

public class AccountTransferCommand extends ObjectCommand<BankAccount> {

    public AccountTransferCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.newBuilder().name("transfer").aliases("pay").create());
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(!(commandSender instanceof OnlineMinecraftPlayer)) {

            return;
        }
        if(args.length < 3) {

            return;
        }
        String receiver0 = args[0];
        String amount0 = args[1];
        String currency0 = args[2];

        BankAccount receiver = DKCoins.getInstance().getAccountManager().searchAccount(receiver0);
        if(receiver == null) {

            return;
        }
        if(!GeneralUtil.isNumber(amount0)) {

            return;
        }
        double amount = Double.parseDouble(amount0);

        Currency currency = DKCoins.getInstance().getCurrencyManager().searchCurrency(currency0);
        if(currency == null) {

            return;
        }
        OnlineMinecraftPlayer player = (OnlineMinecraftPlayer)commandSender;
        AccountMember member = null;
        //account.getMember(DKCoins.getInstance().getUserManager().getUser(player.getId()));
        StringBuilder reasonBuilder = new StringBuilder();
        if(args.length > 3) {
            for (int i = 3; i < args.length; i++) {
                reasonBuilder.append(args[i]);
            }
        } else {
            reasonBuilder = new StringBuilder("none");
        }
        //@Todo property for world, server, location
        boolean success = account.getCredit(currency).transfer(member, amount, receiver.getCredit(currency),
                reasonBuilder.toString(), TransferCause.TRANSFER, new ArrayList<>());
        if(success) {

        } else {

        }
    }
}
