/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 29.01.20, 16:35
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft;

import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.document.annotations.DocumentIgnored;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.commands.UserBankCommand;
import org.mcnative.common.McNative;

public class DKCoinsConfig {

    private static String DEFAULT_CURRENCY_NAME = "Coins";

    @DocumentIgnored
    public static Currency DEFAULT_CURRENCY = null;

    public static int ACCOUNT_USER_START_AMOUNT = 1000;

    public static String[] USER_ACCOUNT_ACCOUNT_CREDIT_ALIASES = new String[]{"coins"};

    public static void init() {
        DEFAULT_CURRENCY = DKCoins.getInstance().getCurrencyManager().searchCurrency(DEFAULT_CURRENCY_NAME);

        for (String userBankCommand : USER_ACCOUNT_ACCOUNT_CREDIT_ALIASES) {
            McNative.getInstance().getLocal().getCommandManager().registerCommand(new UserBankCommand(DKCoinsPlugin.getInstance()
                    , CommandConfiguration.name(userBankCommand)));
        }
    }
}