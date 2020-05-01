/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api;

import net.pretronic.dkcoins.api.account.AccountManager;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.api.currency.CurrencyManager;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.api.user.DKCoinsUserManager;
import net.pretronic.libraries.logging.PretronicLogger;

import java.util.Collection;

public abstract class DKCoins {

    private static DKCoins INSTANCE;

    public abstract PretronicLogger getLogger();

    public abstract AccountManager getAccountManager();

    public abstract CurrencyManager getCurrencyManager();

    public abstract DKCoinsUserManager getUserManager();

    public abstract DKCoinsStorage getStorage();

    public abstract TransactionPropertyBuilder getTransactionPropertyBuilder();

    public abstract TransactionFilter newTransactionFilter();

    public abstract Collection<Migration> getMigrations();

    public abstract Migration getMigration(String name);

    public abstract void registerMigration(Migration migration);


    public static DKCoins getInstance() {
        return INSTANCE;
    }

    public static void setInstance(DKCoins instance) {
        if(INSTANCE != null) throw new IllegalArgumentException("DKCoins instance already set");
        INSTANCE = instance;
    }
}