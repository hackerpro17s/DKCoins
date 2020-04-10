/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.user;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;

import java.util.Collection;
import java.util.UUID;

public class DefaultDKCoinsUser implements DKCoinsUser {

    private final UUID uniqueId;
    private boolean userAccountsLoaded;

    public DefaultDKCoinsUser(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.userAccountsLoaded = false;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public String getName() {
        return getAsPlayer().getName();
    }

    @Override
    public String getDisplayName() {
        return getAsPlayer().getDisplayName();
    }

    @Override
    public Collection<BankAccount> getAccounts() {
        return DKCoins.getInstance().getAccountManager().getAccounts(this);
    }

    @Override
    public BankAccount getDefaultAccount() {
        AccountType accountType = DKCoins.getInstance().getAccountManager().searchAccountType("User");
        return DKCoins.getInstance().getAccountManager().getAccount(getAsPlayer().getName(), accountType);
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

    private MinecraftPlayer getAsPlayer() {
        return McNative.getInstance().getPlayerManager().getPlayer(getUniqueId());
    }

    public BankAccount initAccount() {
        AccountType accountType = DKCoins.getInstance().getAccountManager().searchAccountType("User");
        BankAccount account = DKCoins.getInstance().getAccountManager().getAccount(getAsPlayer().getName(), accountType);
        if(account == null) {
            account = DKCoins.getInstance().getAccountManager().createAccount(getAsPlayer().getName(),
                    accountType, false, null, this);
        }
        return account;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DKCoinsUser && ((DKCoinsUser)obj).getUniqueId().equals(getUniqueId());
    }
}
