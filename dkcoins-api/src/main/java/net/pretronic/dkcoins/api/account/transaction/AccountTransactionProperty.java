/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 31.01.20, 22:01
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.account.transaction;

public interface AccountTransactionProperty {

    String getKey();

    Object asObject();

    String asString();

    int asInt();

    long asLong();

    double asDouble();

    float asFloat();

    byte asByte();
}