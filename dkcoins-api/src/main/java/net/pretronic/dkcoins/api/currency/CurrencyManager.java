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

public interface CurrencyManager {

    Currency getCurrency(int id);

    Currency getCurrency(String name);

    Currency getCurrency(AccountCredit credit);

    Currency createCurrency(String name, String symbol);

    void removeCurrency(Currency currency);

    CurrencyExchangeRate createCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency, double exchangeAmount);

    void removeCurrencyExchangeRate(CurrencyExchangeRate exchangeRate);
}
