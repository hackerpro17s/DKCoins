/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.currency;

import java.util.Collection;

public interface Currency {

    int getId();

    String getName();

    String getSymbol();

    void setName(String name);

    void setSymbol(String name);

    Collection<CurrencyExchangeRate> getExchangeRates();

    CurrencyExchangeRate getExchangeRate(int id);

    CurrencyExchangeRate getExchangeRate(Currency targetCurrency);

    CurrencyExchangeRate setExchangeRate(Currency targetCurrency, double exchangeAmount);

    boolean isTransferDisabled(Currency target);

    double exchange(double amount, Currency target);
}