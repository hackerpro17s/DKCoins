/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 24.11.19, 16:27
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.account;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;

public class DefaultAccountLimitation implements AccountLimitation {

    private final int id;
    private final BankAccount account;
    private final AccountMember member;
    private final AccountMemberRole memberRole;
    private final Currency comparativeCurrency;
    private final CalculationType calculationType;
    private final double amount;
    private final Interval interval;

    public DefaultAccountLimitation(int id, BankAccount account, AccountMember member, AccountMemberRole memberRole,
                                    Currency comparativeCurrency, CalculationType calculationType, double amount, Interval interval) {
        this.id = id;
        this.account = account;
        this.member = member;
        this.memberRole = memberRole;
        this.comparativeCurrency = comparativeCurrency;
        this.calculationType = calculationType;
        this.amount = amount;
        this.interval = interval;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public BankAccount getAccount() {
        return this.account;
    }

    @Override
    public AccountMember getMember() {
        return this.member;
    }

    @Override
    public AccountMemberRole getMemberRole() {
        return this.memberRole;
    }

    @Override
    public Currency getComparativeCurrency() {
        return this.comparativeCurrency;
    }

    @Override
    public CalculationType getCalculationType() {
        return this.calculationType;
    }

    @Override
    public double getAmount() {
        return this.amount;
    }

    @Override
    public String getFormattedAmount() {
        return DKCoins.getInstance().getFormatter().formatCurrencyAmount(getAmount());
    }


    @Override
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AccountLimitation && ((AccountLimitation)obj).getId() == this.id;
    }
}
