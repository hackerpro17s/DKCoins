/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 01.12.19, 14:45
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.currency;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.currency.CurrencyManager;
import net.pretronic.dkcoins.api.events.currency.DKCoinsCurrencyEditEvent;
import net.pretronic.dkcoins.common.SyncAction;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.synchronisation.NetworkSynchronisationCallback;
import net.pretronic.libraries.synchronisation.SynchronisationCaller;
import net.pretronic.libraries.synchronisation.SynchronisationHandler;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.annonations.Internal;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultCurrencyManager implements CurrencyManager, SynchronisationHandler<Currency,Integer>, NetworkSynchronisationCallback {

    private final Collection<Currency> currencies;
    private boolean connected;
    private SynchronisationCaller<Integer> caller;

    public DefaultCurrencyManager() {
        this.currencies = new ArrayList<>();
        this.connected = false;
    }

    @Override
    public Collection<Currency> getCurrencies() {
        if(connected){
            if(this.currencies.isEmpty()){
                this.currencies.addAll(DKCoins.getInstance().getStorage().getCurrencies());
            }
            return this.currencies;
        }
        return DKCoins.getInstance().getStorage().getCurrencies();
    }

    @Internal
    public Collection<Currency> getLoadedCurrencies() {
        return this.currencies;
    }

    @Override
    public Currency getCurrency(int id) {
        if(connected){
            return Iterators.findOne(getCurrencies(), currency -> currency.getId() == id);
        }
        return DKCoins.getInstance().getStorage().getCurrency(id);
    }

    @Override
    public Currency getCurrency(String name) {
        if(connected){
            return Iterators.findOne(getCurrencies(), currency -> currency.getName().equalsIgnoreCase(name));
        }
        return DKCoins.getInstance().getStorage().getCurrency(name);
    }

    @Override
    public Currency getCurrency(AccountCredit credit) {
        return getCurrency(credit.getCurrency().getId());
    }

    @Override
    public Currency searchCurrency(Object identifier) {
        if(connected){
            if(identifier instanceof Integer) return getCurrency((int) identifier);
            else if(identifier instanceof String) return getCurrency((String) identifier);
            throw new IllegalArgumentException("Wrong identifier for currency");
        }
        return DKCoins.getInstance().getStorage().searchCurrency(identifier);
    }

    @Override
    public Currency createCurrency(String name, String symbol) {
        Currency currency = DKCoins.getInstance().getStorage().createCurrency(name, symbol);
        if(connected && !this.currencies.isEmpty()) this.currencies.add(currency);
        for (BankAccount account : DKCoins.getInstance().getAccountManager().getCachedAccounts()) {
            account.addCredit(currency, 0);
        }
        caller.createAndIgnore(currency.getId(), Document.newDocument());
        return currency;
    }

    @Override
    public void updateCurrencyName(Currency currency, String name) {
        DKCoins.getInstance().getStorage().updateCurrencyName(currency.getId(), currency.getName());
        String oldName = currency.getName();
        ((DefaultCurrency)currency).updateName(name);
        caller.updateAndIgnore(currency.getId(), Document.newDocument()
                .add("action", SyncAction.CURRENCY_UPDATE_NAME)
                .add("name", currency.getName()));
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsCurrencyEditEvent(currency, DKCoinsCurrencyEditEvent.Operation.CHANGED_NAME, oldName, name));
    }

    @Override
    public void updateCurrencySymbol(Currency currency, String symbol) {
        DKCoins.getInstance().getStorage().updateCurrencySymbol(currency.getId(), currency.getSymbol());
        String oldSymbol = currency.getSymbol();
        ((DefaultCurrency)currency).updateSymbol(symbol);
        this.caller.updateAndIgnore(currency.getId(), Document.newDocument()
                .add("action", SyncAction.CURRENCY_UPDATE_SYMBOL)
                .add("symbol", currency.getSymbol()));
        DKCoins.getInstance().getEventBus().callEvent(new DKCoinsCurrencyEditEvent(currency, DKCoinsCurrencyEditEvent.Operation.CHANGED_SYMBOL, oldSymbol, symbol));
    }

    @Override
    public void deleteCurrency(Currency currency) {
        DKCoins.getInstance().getStorage().deleteCurrency(currency.getId());
        this.currencies.remove(currency);
        for (BankAccount account : DKCoins.getInstance().getAccountManager().getCachedAccounts()) {
            account.deleteCredit(currency);
        }
        caller.deleteAndIgnore(currency.getId(), Document.newDocument());
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(int id) {
        int currencyId = DKCoins.getInstance().getStorage().getCurrencyExchangeRateCurrencyId(id);
        if(currencyId < 1) return null;
        return getCurrency(currencyId).getExchangeRate(id);
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency) {
        return DKCoins.getInstance().getStorage().getCurrencyExchangeRate(selectedCurrency.getId(), targetCurrency.getId());
    }

    @Override
    public CurrencyExchangeRate createCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency, double exchangeAmount) {
        CurrencyExchangeRate exchangeRate = DKCoins.getInstance().getStorage()
                .createCurrencyExchangeRate(selectedCurrency.getId(), targetCurrency.getId(), exchangeAmount);
        caller.updateAndIgnore(selectedCurrency.getId(), Document.newDocument()
                .add("action", SyncAction.CURRENCY_EXCHANGE_RATE_NEW)
                .add("exchangeRateId", exchangeRate.getId()));
        return exchangeRate;
    }

    @Override
    public void updateCurrencyExchangeRateAmount(CurrencyExchangeRate currencyExchangeRate, double exchangeAmount) {
        DKCoins.getInstance().getStorage().updateCurrencyExchangeAmount(currencyExchangeRate.getCurrency().getId(),
                currencyExchangeRate.getTargetCurrency().getId(), currencyExchangeRate.getExchangeAmount());
        ((DefaultCurrencyExchangeRate)currencyExchangeRate).updateExchangeAmount(exchangeAmount);
        caller.updateAndIgnore(currencyExchangeRate.getCurrency().getId(), Document.newDocument()
                .add("action", SyncAction.CURRENCY_EXCHANGE_RATE_UPDATE_AMOUNT)
                .add("exchangeRateId", currencyExchangeRate.getId())
                .add("exchangeAmount", currencyExchangeRate.getExchangeAmount()));
    }

    @Override
    public void deleteCurrencyExchangeRate(CurrencyExchangeRate exchangeRate) {
        DKCoins.getInstance().getStorage().deleteCurrencyExchangeRate(exchangeRate.getId());
        caller.updateAndIgnore(exchangeRate.getCurrency().getId(), Document.newDocument()
                .add("action", SyncAction.CURRENCY_EXCHANGE_RATE_DELETE)
                .add("exchangeRateId", exchangeRate.getId()));
    }



    @Override
    public void onDelete(Integer id, Document document) {
        if(!this.currencies.isEmpty()){
            Iterators.removeOne(this.currencies, currency -> currency.getId() == id);
        }
    }

    @Override
    public void onCreate(Integer id, Document document) {
        if(connected && !currencies.isEmpty()) {
            this.currencies.add(DKCoins.getInstance().getStorage().getCurrency(id));
        }
    }

    @Override
    public void onUpdate(Integer id, Document document) {
        if(connected && !currencies.isEmpty()) {
            Currency currency = getCurrency(id);
            if(currency != null) ((DefaultCurrency)currency).onUpdate(document);
        }
    }

    @Override
    public void onConnect() {
        this.connected = true;
        this.currencies.clear();
    }

    @Override
    public void onDisconnect() {
        this.connected = false;
        this.currencies.clear();
    }

    @Override
    public SynchronisationCaller<Integer> getCaller() {
        return caller;
    }

    @Override
    public void init(SynchronisationCaller<Integer> synchronisationCaller) {
        this.caller = synchronisationCaller;
        this.connected = synchronisationCaller.isConnected();
    }
}
