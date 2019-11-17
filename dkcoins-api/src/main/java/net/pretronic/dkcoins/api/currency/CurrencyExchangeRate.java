/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:21
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.currency;

public interface CurrencyExchangeRate {

    int getId();

    Currency getSelectedCurrency();

    Currency getTargetCurrency();

    double getExchangeAmount();

    void setExchangeAmount();

    void incrementExchangeAmount(double amount);

    void decrementExchangeAmount(double amount);
}