/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 21:06
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common;

import net.pretronic.databasequery.api.Database;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.DKCoinsFormatter;
import net.pretronic.dkcoins.api.DKCoinsStorage;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.api.user.DKCoinsUserManager;
import net.pretronic.dkcoins.common.account.*;
import net.pretronic.dkcoins.common.account.transaction.DefaultAccountTransaction;
import net.pretronic.dkcoins.common.account.transaction.DefaultTransactionFilter;
import net.pretronic.dkcoins.common.currency.DefaultCurrency;
import net.pretronic.dkcoins.common.currency.DefaultCurrencyExchangeRate;
import net.pretronic.dkcoins.common.currency.DefaultCurrencyManager;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriberRegistry;
import net.pretronic.libraries.utility.Iterators;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultDKCoins extends DKCoins {

    private final PretronicLogger logger;
    private final EventBus eventBus;
    private final DKCoinsStorage storage;
    private final DefaultAccountManager accountManager;
    private final DefaultCurrencyManager currencyManager;
    private final DKCoinsUserManager userManager;
    private final TransactionPropertyBuilder transactionPropertyBuilder;
    private final Collection<Migration> migrations;
    private final DKCoinsFormatter formatter;

    public DefaultDKCoins(PretronicLogger logger, EventBus eventBus, Database database, DKCoinsUserManager userManager, TransactionPropertyBuilder transactionPropertyBuilder, DKCoinsFormatter formatter) {
        DKCoins.setInstance(this);
        this.logger = logger;
        this.eventBus = eventBus;
        this.storage = new DefaultDKCoinsStorage(database);
        this.accountManager = new DefaultAccountManager();
        this.currencyManager = new DefaultCurrencyManager();
        this.userManager = userManager;
        this.transactionPropertyBuilder = transactionPropertyBuilder;
        this.formatter = formatter;
        this.migrations = new ArrayList<>();

        registerVariableDescribers();
    }

    @Override
    public PretronicLogger getLogger() {
        return this.logger;
    }

    @Override
    public EventBus getEventBus() {
        return this.eventBus;
    }

    @Override
    public DefaultAccountManager getAccountManager() {
        return this.accountManager;
    }

    @Override
    public DefaultCurrencyManager getCurrencyManager() {
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

    @Override
    public Collection<Migration> getMigrations() {
        return this.migrations;
    }

    @Override
    public Migration getMigration(String name) {
        return Iterators.findOne(this.migrations, migration -> migration.getName().equalsIgnoreCase(name));
    }

    @Override
    public void registerMigration(Migration migration) {
        this.migrations.add(migration);
    }

    @Override
    public DKCoinsFormatter getFormatter() {
        return formatter;
    }

    public void createDefaults() {
        if(getAccountManager().searchAccountType("Bank") == null) {
            getAccountManager().createAccountType("Bank", "*");
        }
        if(getAccountManager().searchAccountType("User") == null) {
            getAccountManager().createAccountType("User", "");
        }
    }

    private void registerVariableDescribers() {
        VariableDescriberRegistry.registerDescriber(DefaultAccountTransaction.class);
        VariableDescriberRegistry.registerDescriber(DefaultCurrency.class);
        VariableDescriberRegistry.registerDescriber(DefaultCurrencyExchangeRate.class);
        VariableDescriberRegistry.registerDescriber(DefaultBankAccount.class);
        VariableDescriberRegistry.registerDescriber(DefaultAccountMember.class);
        VariableDescriberRegistry.registerDescriber(AccountMemberRole.class);
        VariableDescriberRegistry.registerDescriber(DefaultAccountCredit.class);
        VariableDescriberRegistry.registerDescriber(DefaultAccountLimitation.class);
        VariableDescriberRegistry.registerDescriber(DefaultRankedAccountCredit.class);
    }

    public static DefaultDKCoins getInstance() {
        return (DefaultDKCoins) DKCoins.getInstance();
    }
}