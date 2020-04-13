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

import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransaction;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;

import java.util.Collection;

public class DefaultAccountTransaction implements AccountTransaction {

    private final int id;
    private final AccountCredit source;
    private final AccountMember sender;
    private final AccountCredit receiver;
    private final double amount;
    private final double exchangeRate;
    private final String reason;
    private final String cause;
    private final long time;
    private final Collection<AccountTransactionProperty> properties;

    public DefaultAccountTransaction(int id, AccountCredit source, AccountMember sender, AccountCredit receiver,
                                     double amount, double exchangeRate, String reason, String cause, long time,
                                     Collection<AccountTransactionProperty> properties) {
        this.id = id;
        this.source = source;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.exchangeRate = exchangeRate;
        this.reason = reason;
        this.cause = cause;
        this.time = time;
        this.properties = properties;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public AccountCredit getSource() {
        return this.source;
    }

    @Override
    public AccountMember getSender() {
        return this.sender;
    }

    @Override
    public AccountCredit getReceiver() {
        return this.receiver;
    }

    @Override
    public double getAmount() {
        return this.amount;
    }

    @Override
    public String getFormattedAmount() {
        return DKCoinsConfig.formatCurrencyAmount(getAmount());
    }

    @Override
    public double getExchangeRate() {
        return this.exchangeRate;
    }

    @Override
    public String getFormattedExchangeRate() {
        return DKCoinsConfig.formatCurrencyAmount(getExchangeRate());
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    @Override
    public String getCause() {
        return this.cause;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public String getFormattedTime() {
        return DKCoinsConfig.formatDate(getTime());
    }

    @Override
    public Collection<AccountTransactionProperty> getProperties() {
        return this.properties;
    }

    @Override
    public AccountTransactionProperty getProperty(String key) {
        return Iterators.findOne(this.properties, property -> property.getKey().equalsIgnoreCase(key));
    }
}
