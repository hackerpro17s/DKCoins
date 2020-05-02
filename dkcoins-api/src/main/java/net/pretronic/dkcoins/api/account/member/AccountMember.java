/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 14:44
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account.member;

import net.pretronic.dkcoins.api.account.AccountLimitation;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

import java.util.Collection;

public interface AccountMember {

    int getId();

    BankAccount getAccount();

    DKCoinsUser getUser();

    default String getName() {
        return getUser().getName();
    }

    default String getDisplayName() {
        return getUser().getDisplayName();
    }

    AccountMemberRole getRole();

    void setRole(AccountMemberRole role);

    default boolean canAccess(AccessRight right) {
        return getRole().canAccess(right);
    }

    Collection<AccountLimitation> getLimitations();

    boolean hasLimitation(Currency currency, double amount);

    AccountLimitation getLimitation(Currency comparativeCurrency, double amount, long interval);

    AccountLimitation addLimitation(Currency comparativeCurrency, double amount, long interval);

    boolean removeLimitation(AccountLimitation limitation);

    default boolean removeLimitation(Currency comparativeCurrency, double amount, long interval) {
        return removeLimitation(getLimitation(comparativeCurrency, amount, interval));
    }

    boolean receiveNotifications();

    void setReceiveNotifications(boolean receiveNotifications);
}
