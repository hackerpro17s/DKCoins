/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 22.11.19, 19:44
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.currency;

import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.common.SyncAction;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.utility.annonations.Internal;

public class DefaultCurrencyExchangeRate implements CurrencyExchangeRate {

    private final int id;
    private final Currency currency;
    private final Currency targetCurrency;
    private double exchangeAmount;

    public DefaultCurrencyExchangeRate(int id, Currency currency, Currency targetCurrency, double exchangeAmount) {
        this.id = id;
        this.currency = currency;
        this.targetCurrency = targetCurrency;
        this.exchangeAmount = exchangeAmount;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public Currency getCurrency() {
        return this.currency;
    }

    @Override
    public Currency getTargetCurrency() {
        return this.targetCurrency;
    }

    @Override
    public double getExchangeAmount() {
        return this.exchangeAmount;
    }

    @Override
    public String getFormattedExchangeAmount() {
        return DefaultDKCoins.getInstance().getFormatter().formatCurrencyAmount(getExchangeAmount());
    }

    @Override
    public void setExchangeAmount(double amount) {
        DefaultDKCoins instance = DefaultDKCoins.getInstance();

        instance.getStorage().getCurrencyExchangeRate().update()
                .set("ExchangeAmount", exchangeAmount)
                .where("CurrencyId", currency.getId())
                .where("TargetCurrencyId", targetCurrency.getId())
                .execute();
        updateExchangeAmount(exchangeAmount);

        instance.getCurrencyManager().getCaller().updateAndIgnore(getCurrency().getId(), Document.newDocument()
                .add("action", SyncAction.CURRENCY_EXCHANGE_RATE_UPDATE_AMOUNT)
                .add("exchangeRateId", getId())
                .add("exchangeAmount", getExchangeAmount()));
    }

    @Override
    public void incrementExchangeAmount(double amount) {
        setExchangeAmount(this.exchangeAmount+amount);
    }

    @Override
    public void decrementExchangeAmount(double amount) {
        setExchangeAmount(this.exchangeAmount-amount);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CurrencyExchangeRate && ((CurrencyExchangeRate)obj).getId() == this.id;
    }

    @Internal
    public void updateExchangeAmount(double amount) {
        this.exchangeAmount = amount;
    }
}