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

import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.limitation.LimitationAble;

import java.util.Collection;

public interface AccountMemberRole extends LimitationAble, RoleAble {

    int getId();

    String getName();

    AccountMemberRole getParentRole();

    Collection<AccountMemberRole> getChildRoles();

    Collection<AccessRight> getAccessRights();

    boolean canAccess(AccessRight accessRight);

    boolean isHigher(AccountMemberRole role);

    default boolean isLower(AccountMemberRole role) {
        return !isHigher(role);
    }
}
