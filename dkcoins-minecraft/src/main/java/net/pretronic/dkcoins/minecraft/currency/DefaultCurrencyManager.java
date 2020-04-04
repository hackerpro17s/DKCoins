/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 01.12.19, 14:45
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.currency;

import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.libraries.caching.CacheQuery;
import net.pretronic.libraries.caching.synchronisation.ArraySynchronizableCache;
import net.pretronic.libraries.caching.synchronisation.SynchronizableCache;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.synchronisation.map.HashSynchronizableMap;
import net.pretronic.libraries.synchronisation.map.SynchronizableMap;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.currency.CurrencyManager;
import org.mcnative.common.McNative;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultCurrencyManager implements CurrencyManager {

    private final SynchronizableCache<Currency, Integer> currencyCache;

    public DefaultCurrencyManager() {
        this.currencyCache = new ArraySynchronizableCache<>();
        registerCurrencyQueries();
        for (Currency currency : DKCoins.getInstance().getStorage().getCurrencies()) {
            this.currencyCache.insert(currency);
        }
    }

    @Override
    public Collection<Currency> getCurrencies() {
        return this.currencyCache.getCachedObjects();
    }

    @Override
    public Currency getCurrency(int id) {
        return this.currencyCache.get("byId", id);
    }

    @Override
    public Currency getCurrency(String name) {
        return this.currencyCache.get("search", name);
    }

    @Override
    public Currency getCurrency(AccountCredit credit) {
        return getCurrency(credit.getCurrency().getId());
    }

    @Override
    public Currency searchCurrency(Object identifier) {
        return this.currencyCache.get("search", identifier);
    }

    @Override
    public Currency createCurrency(String name, String symbol) {
        Currency currency = DKCoins.getInstance().getStorage().createCurrency(name, symbol);
        this.currencyCache.insert(currency);
        for (BankAccount account : DKCoins.getInstance().getAccountManager().getCachedAccounts()) {
            account.addCredit(currency, 0);
        }
        this.currencyCache.getCaller().create(currency.getId(), Document.newDocument());
        return currency;
    }

    @Override
    public void updateCurrencyName(Currency currency) {
        DKCoins.getInstance().getStorage().updateCurrencyName(currency.getId(), currency.getName());
        this.currencyCache.getCaller().update(currency.getId(), Document.newDocument().add("name", currency.getName()));
    }

    @Override
    public void updateCurrencySymbol(Currency currency) {
        DKCoins.getInstance().getStorage().updateCurrencySymbol(currency.getId(), currency.getSymbol());
        this.currencyCache.getCaller().update(currency.getId(), Document.newDocument().add("symbol", currency.getSymbol()));
    }

    @Override
    public void deleteCurrency(Currency currency) {
        DKCoins.getInstance().getStorage().deleteCurrency(currency.getId());
        this.currencyCache.remove(currency);
        for (BankAccount account : DKCoins.getInstance().getAccountManager().getCachedAccounts()) {
            account.deleteCredit(currency);
        }
        this.currencyCache.getCaller().delete(currency.getId(), Document.newDocument());
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency) {
        return DKCoins.getInstance().getStorage().getCurrencyExchangeRate(selectedCurrency.getId(), targetCurrency.getId());
    }

    @Override
    public CurrencyExchangeRate createCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency, double exchangeAmount) {
        CurrencyExchangeRate exchangeRate = DKCoins.getInstance().getStorage()
                .createCurrencyExchangeRate(selectedCurrency.getId(), targetCurrency.getId(), exchangeAmount);
        this.currencyCache.getCaller().update(selectedCurrency.getId(), Document.newDocument()
                .add("newExchangeRate", exchangeRate.getId()));
        return exchangeRate;
    }

    @Override
    public void updateCurrencyExchangeRateAmount(CurrencyExchangeRate currencyExchangeRate) {
        DKCoins.getInstance().getStorage().updateCurrencyExchangeAmount(currencyExchangeRate.getCurrency().getId(),
                currencyExchangeRate.getTargetCurrency().getId(), currencyExchangeRate.getExchangeAmount());
        this.currencyCache.getCaller().update(currencyExchangeRate.getCurrency().getId(), Document.newDocument("updateExchangeRateAmount")
                .add("exchangeRateId", currencyExchangeRate.getId())
                .add("exchangeAmount", currencyExchangeRate.getExchangeAmount()));
    }

    @Override
    public void deleteCurrencyExchangeRate(CurrencyExchangeRate exchangeRate) {
        DKCoins.getInstance().getStorage().deleteCurrencyExchangeRate(exchangeRate.getId());
        this.currencyCache.getCaller().update(exchangeRate.getCurrency().getId(), Document.newDocument()
                .add("removeExchangeRate", exchangeRate.getId()));
    }

    private void registerCurrencyQueries() {
        this.currencyCache.setCreateHandler((id, data) -> DKCoins.getInstance().getStorage().getCurrency(id));
        this.currencyCache.registerQuery("search", new CacheQuery<Currency>() {
            @Override
            public boolean check(Currency currency, Object[] identifiers) {
                Object identifier = identifiers[0];
                if(identifier instanceof Integer) {
                    return currency.getId() == (int) identifier;
                }
                return identifier instanceof String && currency.getName().equalsIgnoreCase((String) identifier);
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1, "(Currency cache) Wrong identifier length");
            }
        }).registerQuery("byId", new CacheQuery<Currency>() {
            @Override
            public boolean check(Currency currency, Object[] identifier) {
                return currency.getId() == (int) identifier[0];
            }

            @Override
            public void validate(Object[] identifiers) {
                Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer,
                        "(Currency cache) Wrong identifier length or wrong identifier type");
            }
        });

        McNative.getInstance().getLocal().registerSynchronizingChannel("dkcoins_currency", DKCoinsPlugin.getInstance(),
                int.class, currencyCache);
    }
}
