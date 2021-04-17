/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 17.11.19, 15:04
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.api.user;

import java.util.UUID;

public interface DKCoinsUserManager {

    DKCoinsUser getUser(UUID uniqueId, String name);

    DKCoinsUser getUser(UUID uniqueId);

    DKCoinsUser getUser(String name);
}