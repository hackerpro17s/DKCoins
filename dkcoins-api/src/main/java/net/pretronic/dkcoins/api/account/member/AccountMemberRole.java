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

    OWNER(), //Admin setzen, entfernen
    ADMIN(), //Manager setzen, entfernen
    MANAGER(), //Member hinzufügen, entfernen
    USER(), //abheben
    GUEST() //ansehen
}