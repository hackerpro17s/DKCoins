/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 20.11.19, 15:52
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.currency;

import net.prematic.libraries.utility.Iterators;
import net.prematic.libraries.utility.annonations.Internal;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultCurrency implements Currency {

    private final int id;
    private String name;
    private String symbol;
    private final Collection<CurrencyExchangeRate> exchangeRates;

    public DefaultCurrency(int id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.exchangeRates = new ArrayList<>();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        DKCoins.getInstance().getCurrencyManager().updateCurrencyName(this);
    }

    @Override
    public void setSymbol(String symbol) {
        this.symbol = symbol;
        DKCoins.getInstance().getCurrencyManager().updateCurrencySymbol(this);
    }

    @Override
    public CurrencyExchangeRate getExchangeRate(Currency targetCurrency) {
        CurrencyExchangeRate currencyExchangeRate = Iterators.findOne(this.exchangeRates, exchangeRate -> exchangeRate.getTargetCurrency().equals(targetCurrency));
        if(currencyExchangeRate == null) {
            currencyExchangeRate = DKCoins.getInstance().getCurrencyManager().getCurrencyExchangeRate(this, targetCurrency);
            if(currencyExchangeRate == null) {
                currencyExchangeRate = DKCoins.getInstance().getCurrencyManager().createCurrencyExchangeRate(this, targetCurrency, 1);
            }
            this.exchangeRates.add(currencyExchangeRate);
        }
        return currencyExchangeRate;
    }

    @Override
    public CurrencyExchangeRate setExchangeRate(Currency targetCurrency, double exchangeAmount) {
        CurrencyExchangeRate exchangeRate = getExchangeRate(targetCurrency);
        exchangeRate.setExchangeAmount(exchangeAmount);
        return exchangeRate;
    }

    @Override
    public boolean isTransferDisabled(Currency target) {
        return getExchangeRate(target).getExchangeAmount() == -1;
    }

    @Override
    public double exchange(double amount, Currency target) {
        CurrencyExchangeRate exchangeRate = getExchangeRate(target);
        return exchangeRate == null ? 1 : amount*exchangeRate.getExchangeAmount();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Currency && ((Currency)obj).getId() == getId();
    }

    @Internal
    public void addInternalExchangeRate(CurrencyExchangeRate exchangeRate) {
        this.exchangeRates.add(exchangeRate);
    }
}
