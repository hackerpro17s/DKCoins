/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 20.11.19, 15:33
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.account;

import net.prematic.libraries.utility.annonations.Internal;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultAccountMember implements AccountMember {

    private final int id;
    private final BankAccount account;
    private final DKCoinsUser user;
    private final AccountMemberRole role;
    private final Collection<AccountLimitation> limitations;

    public DefaultAccountMember(int id, BankAccount account, DKCoinsUser user, AccountMemberRole role) {
        this.id = id;
        this.account = account;
        this.user = user;
        this.role = role;
        this.limitations = new ArrayList<>();
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
    public Collection<AccountLimitation> getLimitations() {
        return this.limitations;
    }

    //@Todo caching
    @Override
    public boolean hasLimitation(Currency currency, double amount) {
        return DKCoins.getInstance().getAccountManager().hasLimitation(this, currency, amount);
    }

    @Internal
    public void addLoadedLimitation(AccountLimitation limitation) {
        this.limitations.add(limitation);
    }
}
