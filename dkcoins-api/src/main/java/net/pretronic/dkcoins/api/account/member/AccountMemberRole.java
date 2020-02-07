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

public enum AccountMemberRole {

    OWNER(1), //Admin setzen, entfernen
    ADMIN(2), //Manager setzen, entfernen
    MANAGER(3), //Member hinzufügen, entfernen
    USER(4), //abheben, einzahlen
    GUEST(5); //ansehen

    private final int id;

    AccountMemberRole(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
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
}