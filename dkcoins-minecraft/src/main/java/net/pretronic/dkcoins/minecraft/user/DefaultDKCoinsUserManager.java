/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:19
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.user;

import net.prematic.libraries.caching.ArrayCache;
import net.prematic.libraries.caching.Cache;
import net.prematic.libraries.caching.CacheQuery;
import net.prematic.libraries.utility.Validate;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.api.user.DKCoinsUserManager;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DefaultDKCoinsUserManager implements DKCoinsUserManager {

    private final Cache<DKCoinsUser> coinsUserCache;

    public DefaultDKCoinsUserManager() {
        this.coinsUserCache = new ArrayCache<DKCoinsUser>().setExpireAfterAccess(30, TimeUnit.MINUTES)
                .registerQuery("getOrLoad", new CacheQuery<DKCoinsUser>() {
                    @Override
                    public boolean check(DKCoinsUser user, Object... identifiers) {
                        return user.getUniqueId() == identifiers[0];
                    }

                    @Override
                    public void validate(Object... identifiers) {
                        Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof UUID,
                                "UserCache: Wrong identifiers: %s", Arrays.toString(identifiers));
                    }

                    @Override
                    public DKCoinsUser load(Object... identifiers) {
                        return new DefaultDKCoinsUser((UUID) identifiers[0]);
                    }
                });
    }

    @Override
    public DKCoinsUser getUser(UUID uniqueId) {
        return this.coinsUserCache.get("getOrLoad", uniqueId);
    }

    @Override
    public DKCoinsUser getUser(String name) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(name);
        if(player != null) {
            return getUser(player.getUniqueId());
        }
        return null;
    }
}
