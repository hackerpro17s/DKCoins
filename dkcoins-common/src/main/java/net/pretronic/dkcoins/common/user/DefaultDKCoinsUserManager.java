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

import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.api.user.DKCoinsUserManager;
import net.pretronic.libraries.caching.ArrayCache;
import net.pretronic.libraries.caching.Cache;
import net.pretronic.libraries.caching.ShadowArrayCache;
import net.pretronic.libraries.utility.annonations.Internal;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DefaultDKCoinsUserManager implements DKCoinsUserManager {

    private final Cache<DKCoinsUser> coinsUserCache;

    public DefaultDKCoinsUserManager() {
        this.coinsUserCache = new ShadowArrayCache<DKCoinsUser>()
                .setExpireAfterAccess(30, TimeUnit.MINUTES)
                .setMaxSize(500);
    }

    @Internal
    public Cache<DKCoinsUser> getUserCache() {
        return coinsUserCache;
    }

    @Override
    public DKCoinsUser getUser(UUID uniqueId, String name) {
        return this.coinsUserCache.get("byUUIDAndName", uniqueId);
    }

    @Override
    public DKCoinsUser getUser(UUID uniqueId) {
        return this.coinsUserCache.get("byUUID", uniqueId);
    }

    @Override
    public DKCoinsUser getUser(String name) {
        return this.coinsUserCache.get("byName", name);
    }

}
