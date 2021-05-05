/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.common.user;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.utility.Validate;

import java.util.Collection;
import java.util.UUID;

public class DefaultDKCoinsUser implements DKCoinsUser {

    private final UUID uniqueId;
    private final String name;
    private boolean userAccountsLoaded;

    public DefaultDKCoinsUser(UUID uniqueId, String name) {
        Validate.notNull(uniqueId, name);
        this.name = name;
        this.uniqueId = uniqueId;
        this.userAccountsLoaded = false;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<BankAccount> getAccounts() {
        return DKCoins.getInstance().getAccountManager().getAccounts(this);
    }

    @Override
    public BankAccount getDefaultAccount() {
        AccountType accountType = DKCoins.getInstance().getAccountManager().searchAccountType("User");
        BankAccount account = DKCoins.getInstance().getAccountManager().getAccountByOwner(getUniqueId(), accountType);
        if(account == null) {
            account = DKCoins.getInstance().getAccountManager().createAccount(getName(), accountType, false, null, this);
        }
        return account;
    }

    @Override
    public AccountMember getAsMember(BankAccount account) {
        return account.getMember(this);
    }

    public boolean isUserAccountsLoaded() {
        return userAccountsLoaded;
    }

    public DefaultDKCoinsUser setUserAccountsLoaded(boolean userAccountsLoaded) {
        this.userAccountsLoaded = userAccountsLoaded;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DKCoinsUser && ((DKCoinsUser)obj).getUniqueId().equals(getUniqueId());
    }
}
