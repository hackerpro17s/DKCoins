/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 15.02.20, 21:35
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.account.transaction;

import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.transaction.TransactionFilter;
import net.pretronic.dkcoins.api.currency.Currency;

public class DefaultTransactionFilter implements TransactionFilter {

    private BankAccount account;
    private String world;
    private String server;
    private long time;
    private BankAccount receiver;
    private Currency currency;
    private String reason;
    private String cause;
    private int page;
    
    @Override
    public TransactionFilter account(BankAccount account) {
        this.account = account;
        return this;
    }

    @Override
    public TransactionFilter world(String world) {
        this.world = world;
        return this;
    }

    @Override
    public TransactionFilter server(String server) {
        this.server = server;
        return this;
    }

    @Override
    public TransactionFilter time(long time) {
        this.time = time;
        return this;
    }

    @Override
    public TransactionFilter receiver(BankAccount receiver) {
        this.receiver = receiver;
        return this;
    }

    @Override
    public TransactionFilter currency(Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public TransactionFilter reason(String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public TransactionFilter cause(String cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public TransactionFilter page(int page) {
        this.page = page;
        return this;
    }

    @Internal
    public BankAccount getAccount() {
        return account;
    }

    @Internal
    public String getWorld() {
        return world;
    }

    @Internal
    public String getServer() {
        return server;
    }

    @Internal
    public long getTime() {
        return time;
    }

    @Internal
    public BankAccount getReceiver() {
        return receiver;
    }

    @Internal
    public Currency getCurrency() {
        return currency;
    }

    @Internal
    public String getReason() {
        return reason;
    }

    @Internal
    public String getCause() {
        return cause;
    }

    @Internal
    public int getPage() {
        return page;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DefaultTransactionFilter)) return false;
        DefaultTransactionFilter filter = (DefaultTransactionFilter) obj;
        return filter.account.equals(this.account)
                && filter.world.equals(this.world)
                && filter.server.equals(this.server)
                && filter.time == this.time
                && filter.receiver.equals(this.receiver)
                && filter.currency.equals(this.currency)
                && filter.reason.equals(this.reason)
                && filter.cause.equals(this.cause)
                && filter.page == this.page;
    }
}
