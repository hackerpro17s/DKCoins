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

import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.dkcoins.api.account.AccountManager;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.api.currency.CurrencyManager;
import net.pretronic.dkcoins.api.user.DKCoinsUserManager;

import java.util.Collection;

public interface DKCoins {

    PretronicLogger getLogger();

    AccountManager getAccountManager();

    CurrencyManager getCurrencyManager();

    DKCoinsUserManager getUserManager();

    DKCoinsStorage getStorage();

    TransactionPropertyBuilder getTransactionPropertyBuilder();

    TransactionFilter newTransactionFilter();

    Collection<Migration> getMigrations();

    Migration getMigration(String name);

    void registerMigration(Migration migration);


    static DKCoins getInstance() {
        return InstanceHolder.INSTANCE;
    }

    static void setInstance(DKCoins instance) {
        InstanceHolder.INSTANCE = instance;
    }

    class InstanceHolder {
        public static DKCoins INSTANCE;
    }
}