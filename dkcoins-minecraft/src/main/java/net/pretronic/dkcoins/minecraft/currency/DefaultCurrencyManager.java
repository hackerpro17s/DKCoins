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

import net.prematic.libraries.caching.ArrayCache;
import net.prematic.libraries.caching.Cache;
import net.prematic.libraries.caching.CacheQuery;
import net.prematic.libraries.utility.Validate;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.currency.CurrencyManager;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class DefaultCurrencyManager implements CurrencyManager {

    private final Cache<Currency> currencyCache;

    public DefaultCurrencyManager() {
        this.currencyCache = new ArrayCache<Currency>().setExpireAfterAccess(1, TimeUnit.HOURS).setMaxSize(1000)
                .registerQuery("byId", new CacheQuery<Currency>() {
                    @Override
                    public boolean check(Currency currency, Object[] identifiers) {
                        return currency.getId() == (int) identifiers[0];
                    }

                    @Override
                    public void validate(Object[] identifiers) {
                        Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof Integer,
                                "CurrencyCache: Wrong identifiers: %s", Arrays.toString(identifiers));
                    }

                    @Override
                    public Currency load(Object[] identifiers) {
                        return DKCoins.getInstance().getStorage().getCurrency((int) identifiers[0]);
                    }
                })
                .registerQuery("byName", new CacheQuery<Currency>() {
                    @Override
                    public boolean check(Currency currency, Object[] identifiers) {
                        return currency.getName().equalsIgnoreCase((String) identifiers[0]);
                    }

                    @Override
                    public void validate(Object[] identifiers) {
                        Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof String,
                                "CurrencyCache: Wrong identifiers: %s", Arrays.toString(identifiers));
                    }

                    @Override
                    public Currency load(Object[] identifiers) {
                        return DKCoins.getInstance().getStorage().getCurrency((String) identifiers[0]);
                    }
                });
    }

    @Override
    public Currency getCurrency(int id) {
        return this.currencyCache.get("byId", id);
    }

    @Override
    public Currency getCurrency(String name) {
        return this.currencyCache.get("byName", name);
    }

    @Override
    public Currency getCurrency(AccountCredit credit) {
        return getCurrency(credit.getCurrency().getId());
    }

    @Override
    public Currency searchCurrency(Object identifier) {
        return null;
    }

    @Override
    public Currency createCurrency(String name, String symbol) {
        Currency currency = DKCoins.getInstance().getStorage().createCurrency(name, symbol);
        this.currencyCache.insert(currency);
        return currency;
    }

    @Override
    public void updateCurrencyName(Currency currency) {
        DKCoins.getInstance().getStorage().updateCurrencyName(currency.getId(), currency.getName());
    }

    @Override
    public void updateCurrencySymbol(Currency currency) {
        DKCoins.getInstance().getStorage().updateCurrencySymbol(currency.getId(), currency.getSymbol());
    }

    @Override
    public void deleteCurrency(Currency currency) {
        DKCoins.getInstance().getStorage().deleteCurrency(currency.getId());
        this.currencyCache.remove(currency);
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency) {
        return null;
    }

    @Override
    public CurrencyExchangeRate addCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency, double exchangeAmount) {
        return DKCoins.getInstance().getStorage().addCurrencyExchangeRate(selectedCurrency.getId(), targetCurrency.getId(), exchangeAmount);
    }

    @Override
    public void updateCurrencyExchangeRateAmount(CurrencyExchangeRate currencyExchangeRate) {

    }

    @Override
    public void deleteCurrencyExchangeRate(CurrencyExchangeRate exchangeRate) {
        DKCoins.getInstance().getStorage().deleteCurrencyExchangeRate(exchangeRate.getId());
    }
}
