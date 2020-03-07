/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 15:00
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.currency;

import net.pretronic.dkcoins.api.account.AccountCredit;

import java.util.Collection;

public interface CurrencyManager {

    Collection<Currency> getCurrencies();

    Currency getCurrency(int id);

    Currency getCurrency(String name);

    Currency getCurrency(AccountCredit credit);

    Currency searchCurrency(Object identifier);

    Currency createCurrency(String name, String symbol);

    void updateCurrencyName(Currency currency);

    void updateCurrencySymbol(Currency currency);

    void deleteCurrency(Currency currency);


    CurrencyExchangeRate getCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency);

    CurrencyExchangeRate createCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency, double exchangeAmount);

    void updateCurrencyExchangeRateAmount(CurrencyExchangeRate currencyExchangeRate);

    void deleteCurrencyExchangeRate(CurrencyExchangeRate exchangeRate);
}
