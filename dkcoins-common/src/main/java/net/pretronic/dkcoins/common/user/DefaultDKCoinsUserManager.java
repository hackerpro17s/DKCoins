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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class DefaultDKCoinsUserManager implements DKCoinsUserManager {

    protected final Cache<DKCoinsUser> coinsUserCache;

    public DefaultDKCoinsUserManager() {
        this.coinsUserCache = new ArrayCache<DKCoinsUser>()
                .setExpireAfterAccess(30, TimeUnit.MINUTES)
                .setMaxSize(500);
    }

    @Override
    public DKCoinsUser getUser(UUID uniqueId) {
        return this.coinsUserCache.get("getOrLoad", uniqueId);
    }

}
