/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 21:06
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft;

import net.prematic.libraries.logging.PrematicLogger;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountManager;
import net.pretronic.dkcoins.api.currency.CurrencyManager;
import net.pretronic.dkcoins.api.DKCoinsStorage;
import net.pretronic.dkcoins.api.user.DKCoinsUserManager;
import net.pretronic.dkcoins.minecraft.account.DefaultAccountManager;
import net.pretronic.dkcoins.minecraft.currency.DefaultCurrencyManager;
import net.pretronic.dkcoins.minecraft.user.DefaultDKCoinsUserManager;
import org.mcnative.common.McNative;

public class MinecraftDKCoins implements DKCoins {

    private final PrematicLogger logger;
    private final AccountManager accountManager;
    private final CurrencyManager currencyManager;
    private final DKCoinsUserManager userManager;
    private final DKCoinsStorage storage;

    MinecraftDKCoins() {
        this.logger = McNative.getInstance().getLogger();
        this.accountManager = new DefaultAccountManager();
        this.currencyManager = new DefaultCurrencyManager();
        this.userManager = new DefaultDKCoinsUserManager();
        this.storage = new DefaultDKCoinsStorage(null);
    }

    @Override
    public PrematicLogger getLogger() {
        return this.logger;
    }

    @Override
    public AccountManager getAccountManager() {
        return this.accountManager;
    }

    @Override
    public CurrencyManager getCurrencyManager() {
        return this.currencyManager;
    }

    @Override
    public DKCoinsUserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public DKCoinsStorage getStorage() {
        return this.storage;
    }
}
