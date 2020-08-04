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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public enum AccountMemberRole {

    GUEST(5, null, AccessRight.VIEW),
    USER(4, GUEST, AccessRight.WITHDRAW, AccessRight.DEPOSIT),
    MANAGER(3, USER, AccessRight.MEMBER_MANAGEMENT, AccessRight.EXCHANGE),
    ADMIN(2, MANAGER, AccessRight.LIMIT_MANAGEMENT, AccessRight.ROLE_MANAGEMENT),
    OWNER(1, ADMIN, AccessRight.ADMIN_MANAGEMENT, AccessRight.DELETE);

    private final int id;
    private final AccountMemberRole childRole;
    private final AccessRight[] accessRights;

    AccountMemberRole(int id, AccountMemberRole childRole, AccessRight... accessRights) {
        this.id = id;
        this.childRole = childRole;
        this.accessRights = initAccessRights(accessRights);
    }

    public int getId() {
        return id;
    }

    public AccountMemberRole getChildRole() {
        return childRole;
    }

    public AccessRight[] getAccessRights() {
        return accessRights;
    }

    //@Todo überarbeiten
    public String getName() {
        return name();
    }

    public boolean canAccess(AccessRight accessRight) {
        for (AccessRight right : getAccessRights()) {
            if(right == accessRight) return true;
        }
        return false;
    }

    public boolean isHigher(AccountMemberRole role) {
        return this.id < role.getId();
    }

    public static AccountMemberRole byId(int id) {
        for (AccountMemberRole role : AccountMemberRole.values()) {
            if(role.getId() == id) return role;
        }
        throw new IllegalArgumentException(String.format("AccountMemberRole with id %s doesn't exist", id));
    }

    public static AccountMemberRole byIdOrNull(int id) {
        for (AccountMemberRole role : AccountMemberRole.values()) {
            if(role.getId() == id) return role;
        }
        return null;
    }

    public static AccountMemberRole byName(String name) {
        for (AccountMemberRole value : AccountMemberRole.values()) {
            if(value.name().equalsIgnoreCase(name)) return value;
        }
        return null;
    }

    private AccessRight[] initAccessRights(AccessRight[] accessRights0) {
        Collection<AccessRight> accessRights = new ArrayList<>();
        accessRights.addAll(Arrays.asList(accessRights0));
        if(this.childRole != null) {
            AccountMemberRole role = this.childRole;
            while (role != null) {
                accessRights.addAll(Arrays.asList(role.accessRights));
                role = role.childRole;
            }
        }
        return accessRights.toArray(new AccessRight[0]);
    }
}