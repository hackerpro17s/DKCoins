/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 20.11.19, 15:33
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
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.annonations.Internal;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultAccountMember implements AccountMember {

    private final int id;
    private final BankAccount account;
    private final DKCoinsUser user;
    private AccountMemberRole role;
    private boolean receiveNotifications;

    public DefaultAccountMember(int id, BankAccount account, DKCoinsUser user, AccountMemberRole role, boolean receiveNotifications) {
        this.id = id;
        this.account = account;
        this.user = user;
        this.role = role;
        this.receiveNotifications = receiveNotifications;
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
    public DKCoinsUser getUser() {
        return this.user;
    }

    @Override
    public AccountMemberRole getRole() {
        return this.role;
    }

    @Override
    public void setRole(AccountMemberRole role) {
        DKCoins.getInstance().getAccountManager().updateAccountMemberRole(this, role);
    }

    @Override
    public Collection<AccountLimitation> getLimitations() {
        return Iterators.filter(getAccount().getLimitations(), limitation -> limitation.getMember() != null && limitation.getMember().getId() == getId());
    }


    @Override
    public AccountLimitation getLimitation(Currency comparativeCurrency, double amount, AccountLimitation.Interval interval) {
        return Iterators.findOne(getAccount().getLimitations(), limitation -> {
            if(limitation.getMember() == null || limitation.getMember().getId() != getId()) return false;
            if(!comparativeCurrency.equals(limitation.getComparativeCurrency())) return false;
            if(amount != limitation.getAmount()) return false;
            if(interval != limitation.getInterval()) return false;
            return true;
        });
    }

    @Override
    public boolean hasLimitation(Currency currency, double amount) {
        return getAccount().hasLimitation(this, currency, amount);
    }

    @Override
    public boolean receiveNotifications() {
        return this.receiveNotifications;
    }

    @Override
    public void setReceiveNotifications(boolean receiveNotifications) {
        DKCoins.getInstance().getAccountManager().updateAccountMemberReceiveNotifications(this, receiveNotifications);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AccountMember && ((AccountMember)obj).getId() == getId();
    }

    @Internal
    public void updateRole(AccountMemberRole role) {
        this.role = role;
    }

    @Internal
    public void updateReceiveNotifications(boolean receiveNotifications) {
        this.receiveNotifications = receiveNotifications;
    }
}
