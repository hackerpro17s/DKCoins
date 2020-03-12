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

import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountManager;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.api.currency.CurrencyManager;
import net.pretronic.dkcoins.api.DKCoinsStorage;
import net.pretronic.dkcoins.api.user.DKCoinsUserManager;
import net.pretronic.dkcoins.minecraft.account.DefaultAccountManager;
import net.pretronic.dkcoins.minecraft.account.transaction.DefaultTransactionFilter;
import net.pretronic.dkcoins.minecraft.currency.DefaultCurrencyManager;
import net.pretronic.dkcoins.minecraft.user.DefaultDKCoinsUserManager;
import org.mcnative.common.McNative;
import org.mcnative.common.plugin.configuration.ConfigurationProvider;

public class MinecraftDKCoins implements DKCoins {

    private final PretronicLogger logger;
    private final DKCoinsStorage storage;
    private final AccountManager accountManager;
    private final CurrencyManager currencyManager;
    private final DKCoinsUserManager userManager;
    private final TransactionPropertyBuilder transactionPropertyBuilder;

    MinecraftDKCoins(TransactionPropertyBuilder transactionPropertyBuilder) {
        DKCoins.setInstance(this);
        this.logger = McNative.getInstance().getLogger();
        this.storage = new DefaultDKCoinsStorage(McNative.getInstance().getPluginManager().getService(ConfigurationProvider.class)
                .getDatabase(DKCoinsPlugin.getInstance(), "default", true));
        this.accountManager = new DefaultAccountManager();
        this.currencyManager = new DefaultCurrencyManager();
        this.userManager = new DefaultDKCoinsUserManager();
        this.transactionPropertyBuilder = transactionPropertyBuilder;

        createDefaultAccountTypes();
        DKCoinsConfig.init();
    }

    @Override
    public PretronicLogger getLogger() {
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

    @Override
    public TransactionPropertyBuilder getTransactionPropertyBuilder() {
        return this.transactionPropertyBuilder;
    }

    @Override
    public TransactionFilter newTransactionFilter() {
        return new DefaultTransactionFilter();
    }

    private void createDefaultAccountTypes() {
        if(getAccountManager().searchAccountType("Bank") == null) {
            getAccountManager().createAccountType("Bank", "*");
        }
        if(getAccountManager().searchAccountType("User") == null) {
            getAccountManager().createAccountType("User", "");
        }

        if(getCurrencyManager().searchCurrency("Coins") == null) {
            getCurrencyManager().createCurrency("Coins", "$");
        }
    }
}
