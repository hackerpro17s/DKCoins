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

import net.prematic.libraries.utility.Iterators;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.currency.CurrencyManager;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultCurrencyManager implements CurrencyManager {

    private final Collection<Currency> currencies;

    public DefaultCurrencyManager() {
        this.currencies = new ArrayList<>();
        this.currencies.addAll(DKCoins.getInstance().getStorage().getCurrencies());
    }

    @Override
    public Collection<Currency> getCurrencies() {
        return this.currencies;
    }

    @Override
    public Currency getCurrency(int id) {
        return Iterators.findOne(this.currencies, currency -> currency.getId() == id);
    }

    @Override
    public Currency getCurrency(String name) {
        return Iterators.findOne(this.currencies, currency -> currency.getName().equalsIgnoreCase(name));
    }

    @Override
    public Currency getCurrency(AccountCredit credit) {
        return getCurrency(credit.getCurrency().getId());
    }

    @Override
    public Currency searchCurrency(Object identifier) {
        return Iterators.findOne(this.currencies, currency -> {
            if(identifier instanceof Integer) return currency.getId() == (int) identifier;
            return identifier instanceof String && (currency.getName().equalsIgnoreCase((String) identifier) ||
                    currency.getSymbol().equalsIgnoreCase((String) identifier));
        });
    }

    @Override
    public Currency createCurrency(String name, String symbol) {
        Currency currency = DKCoins.getInstance().getStorage().createCurrency(name, symbol);
        this.currencies.add(currency);
        for (BankAccount account : DKCoins.getInstance().getAccountManager().getCachedAccounts()) {
            account.addCredit(currency, 0);
        }
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
        this.currencies.remove(currency);
        for (BankAccount account : DKCoins.getInstance().getAccountManager().getCachedAccounts()) {
            account.deleteCredit(currency);
        }
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency) {
        return DKCoins.getInstance().getStorage().getCurrencyExchangeRate(selectedCurrency.getId(), targetCurrency.getId());
    }

    @Override
    public CurrencyExchangeRate createCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency, double exchangeAmount) {
        return DKCoins.getInstance().getStorage().createCurrencyExchangeRate(selectedCurrency.getId(), targetCurrency.getId(), exchangeAmount);
    }

    @Override
    public void updateCurrencyExchangeRateAmount(CurrencyExchangeRate currencyExchangeRate) {
        DKCoins.getInstance().getStorage().updateCurrencyExchangeAmount(currencyExchangeRate.getCurrency().getId(),
                currencyExchangeRate.getTargetCurrency().getId(), currencyExchangeRate.getExchangeAmount());
    }

    @Override
    public void deleteCurrencyExchangeRate(CurrencyExchangeRate exchangeRate) {
        DKCoins.getInstance().getStorage().deleteCurrencyExchangeRate(exchangeRate.getId());
    }
}
