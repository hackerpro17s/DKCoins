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

import net.pretronic.dkcoins.minecraft.SyncAction;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.entry.DocumentEntry;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.libraries.document.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        System.out.println("ssymbol:" + symbol);
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
    public Collection<CurrencyExchangeRate> getExchangeRates() {
        Collection<CurrencyExchangeRate> exchangeRates = new ArrayList<>();
        for (Currency currency : DKCoins.getInstance().getCurrencyManager().getCurrencies()) {
            exchangeRates.add(getExchangeRate(currency));
        }
        return exchangeRates;
    }

    @Override
    public CurrencyExchangeRate getExchangeRate(int id) {
        CurrencyExchangeRate exchangeRate = Iterators.findOne(this.exchangeRates, exchangeRate0 -> exchangeRate0.getId() == id);
        if(exchangeRate == null) {
            exchangeRate = DKCoins.getInstance().getCurrencyManager().getCurrencyExchangeRate(id);
        }
        return exchangeRate;
    }

    @Override
    public CurrencyExchangeRate getExchangeRate(Currency targetCurrency) {
        Validate.notNull(targetCurrency);
        CurrencyExchangeRate exchangeRate = Iterators.findOne(this.exchangeRates, exchangeRate0 ->
                exchangeRate0.getTargetCurrency().equals(targetCurrency));
        if(exchangeRate == null) {
            exchangeRate = DKCoins.getInstance().getCurrencyManager().getCurrencyExchangeRate(this, targetCurrency);
            if(exchangeRate == null) {
                exchangeRate = DKCoins.getInstance().getCurrencyManager().createCurrencyExchangeRate(this, targetCurrency, 1);
            }
        }
        return exchangeRate;
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
    public void addLoadedExchangeRate(CurrencyExchangeRate exchangeRate) {
        this.exchangeRates.add(exchangeRate);
    }

    @Override
    public void onUpdate(Document data) {
        switch (data.getString("action")) {
            case SyncAction.CURRENCY_UPDATE_NAME: {
                this.name = data.getString("name");
                break;
            }
            case SyncAction.CURRENCY_UPDATE_SYMBOL: {
                this.symbol = data.getString("symbol");
                break;
            }
            case SyncAction.CURRENCY_EXCHANGE_RATE_NEW: {
                addLoadedExchangeRate(DKCoins.getInstance().getStorage().getCurrencyExchangeRate(data.getInt("exchangeRateId")));
                break;
            }
            case SyncAction.CURRENCY_EXCHANGE_RATE_DELETE: {
                Iterators.removeOne(this.exchangeRates, exchangeRate -> exchangeRate.getId() == data.getInt("exchangeRateId"));
                break;
            }
            case SyncAction.CURRENCY_EXCHANGE_RATE_UPDATE_AMOUNT: {
                DefaultCurrencyExchangeRate exchangeRate = (DefaultCurrencyExchangeRate) getExchangeRate(data.getInt("exchangeRateId"));
                exchangeRate.updateExchangeAmount(data.getDouble("exchangeAmount"));
                break;
            }
        }

    }
}
