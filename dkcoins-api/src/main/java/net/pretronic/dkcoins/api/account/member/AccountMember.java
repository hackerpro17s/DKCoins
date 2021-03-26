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

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.limitation.LimitationAble;
import net.pretronic.dkcoins.api.user.DKCoinsUser;

public interface AccountMember extends LimitationAble, RoleAble {

    int getId();

    BankAccount getAccount();

    DKCoinsUser getUser();

    default String getName() {
        return getUser().getName();
    }

    default String getDisplayName() {
        return getUser().getDisplayName();
    }

    void setRole(AccountMemberRole role);

    default boolean canAccess(AccessRight right) {
        return getRole().canAccess(right);
    }

    boolean receiveNotifications();

    void setReceiveNotifications(boolean receiveNotifications);
}
