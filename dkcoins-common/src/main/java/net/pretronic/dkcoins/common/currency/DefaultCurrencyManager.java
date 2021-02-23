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

import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.currency.CurrencyExchangeRate;
import net.pretronic.dkcoins.api.currency.CurrencyManager;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.common.SyncAction;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.synchronisation.NetworkSynchronisationCallback;
import net.pretronic.libraries.synchronisation.SynchronisationCaller;
import net.pretronic.libraries.synchronisation.SynchronisationHandler;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
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
            if(this.currencies.isEmpty()) this.currencies.addAll(loadCurrencies());
            return this.currencies;
        }
        return loadCurrencies();
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
        return getCurrencyInternal(id);
    }

    @Override
    public Currency getCurrency(String name) {
        if(connected){
            return Iterators.findOne(getCurrencies(), currency -> currency.getName().equalsIgnoreCase(name));
        }
        return getCurrencyInternal(name);
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
        return searchCurrencyInternal(identifier);
    }

    @Override
    public Currency createCurrency(String name, String symbol) {
        int id = DefaultDKCoins.getInstance().getStorage().getCurrency().insert()
                .set("Name", name)
                .set("Symbol", symbol)
                .executeAndGetGeneratedKeyAsInt("Id");

        Currency currency = new DefaultCurrency(id, name, symbol);
        if(connected && !this.currencies.isEmpty()) this.currencies.add(currency);

        for (BankAccount account : DKCoins.getInstance().getAccountManager().getCachedAccounts()) {
            account.addCredit(currency, 0);
        }
        caller.createAndIgnore(currency.getId(), Document.newDocument());
        return currency;
    }

    @Override
    public void deleteCurrency(Currency currency) {
        DefaultDKCoins.getInstance().getStorage().getCurrency().delete()
                .where("Id", currency.getId())
                .execute();

        this.currencies.remove(currency);
        for (BankAccount account : DKCoins.getInstance().getAccountManager().getCachedAccounts()) {
            account.deleteCredit(currency);
        }
        caller.deleteAndIgnore(currency.getId(), Document.newDocument());
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(int id) {
        int currencyId = getCurrencyExchangeRateCurrencyId(id);
        if(currencyId < 1) return null;
        return getCurrency(currencyId).getExchangeRate(id);
    }

    @Override
    public CurrencyExchangeRate getCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency) {
        Validate.notNull(selectedCurrency, targetCurrency);
        return getCurrencyExchangeRateInternal(selectedCurrency, targetCurrency);
    }

    @Override
    public CurrencyExchangeRate createCurrencyExchangeRate(Currency selectedCurrency, Currency targetCurrency, double exchangeAmount) {
        int id = DefaultDKCoins.getInstance().getStorage().getCurrencyExchangeRate().insert()
                .set("CurrencyId", selectedCurrency.getId())
                .set("TargetCurrencyId", targetCurrency.getId())
                .set("ExchangeAmount", exchangeAmount)
                .executeAndGetGeneratedKeyAsInt("Id");

        CurrencyExchangeRate exchangeRate = new DefaultCurrencyExchangeRate(id, selectedCurrency, targetCurrency, exchangeAmount);
        caller.updateAndIgnore(selectedCurrency.getId(), Document.newDocument()
                .add("action", SyncAction.CURRENCY_EXCHANGE_RATE_NEW)
                .add("exchangeRateId", exchangeRate.getId()));
        return exchangeRate;
    }

    @Override
    public void deleteCurrencyExchangeRate(CurrencyExchangeRate exchangeRate) {
        DefaultDKCoins.getInstance().getStorage().getCurrencyExchangeRate().delete().where("Id", exchangeRate.getId()).execute();
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
            for (Currency currency : this.currencies) {
                if(currency.getId() == id) return;
            }
            this.currencies.add(getCurrencyInternal(id));
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

    private Collection<Currency> loadCurrencies() {
        Collection<Currency> currencies = new ArrayList<>();
        DefaultDKCoins.getInstance().getStorage().getCurrency().find().execute().loadIn(currencies, entry -> new DefaultCurrency(
                entry.getInt("Id"),
                entry.getString("Name"),
                entry.getString("Symbol")));
        return currencies;
    }

    private Currency getCurrencyInternal(int id) {
        return getCurrencyInternal(DefaultDKCoins.getInstance().getStorage().getCurrency().find()
                .where("Id", id)
                .execute().firstOrNull());
    }

    private Currency getCurrencyInternal(String name) {
        return getCurrencyInternal(DefaultDKCoins.getInstance().getStorage().getCurrency().find()
                .where("Name", name)
                .execute().firstOrNull());
    }

    private Currency searchCurrencyInternal(Object identifier) {
        QueryResultEntry result = DefaultDKCoins.getInstance().getStorage().getCurrency().find()
                .or(query -> {
                    if(identifier instanceof Integer) query.where("Id", identifier);
                    query.where("Name", identifier).where("Symbol", identifier);
                }).execute().firstOrNull();
        return getCurrencyInternal(result);
    }

    private Currency getCurrencyInternal(QueryResultEntry result) {
        if(result == null) return null;
        return new DefaultCurrency(result.getInt("Id"),
                result.getString("Name"),
                result.getString("Symbol"));
    }

    private CurrencyExchangeRate getCurrencyExchangeRateInternal(int id) {
        QueryResultEntry entry = DefaultDKCoins.getInstance().getStorage().getCurrencyExchangeRate().find()
                .where("Id", id)
                .execute().firstOrNull();
        if(entry == null) return null;
        return new DefaultCurrencyExchangeRate(id,
                DKCoins.getInstance().getCurrencyManager().getCurrency("CurrencyId"),
                DKCoins.getInstance().getCurrencyManager().getCurrency("TargetCurrencyId"),
                entry.getDouble("ExchangeAmount"));
    }

    private CurrencyExchangeRate getCurrencyExchangeRateInternal(Currency currency, Currency targetCurrency) {
        QueryResultEntry entry = DefaultDKCoins.getInstance().getStorage().getCurrencyExchangeRate().find()
                .where("CurrencyId", currency.getId())
                .where("TargetCurrencyId", targetCurrency.getId())
                .execute().firstOrNull();
        if(entry == null) return null;
        return new DefaultCurrencyExchangeRate(entry.getInt("Id"), currency, targetCurrency, entry.getDouble("ExchangeAmount"));
    }

    private int getCurrencyExchangeRateCurrencyId(int id) {
        QueryResultEntry result = DefaultDKCoins.getInstance().getStorage().getCurrencyExchangeRate().find()
                .where("Id", id)
                .execute().firstOrNull();
        if(result == null) return -1;
        return result.getInt("CurrencyId");
    }
}
