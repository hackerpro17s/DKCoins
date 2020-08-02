/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 02.08.20, 20:44
 * @web %web%
 *
 * The DKCoins Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.pretronic.dkcoins.minecraft.user;

import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.user.DefaultDKCoinsUserManager;
import net.pretronic.libraries.caching.CacheQuery;
import net.pretronic.libraries.utility.Validate;
import org.mcnative.common.McNative;
import org.mcnative.common.player.MinecraftPlayer;

import java.util.Arrays;
import java.util.UUID;

public class MinecraftDKCoinsUserManager extends DefaultDKCoinsUserManager {

    public MinecraftDKCoinsUserManager() {
        this.coinsUserCache.registerQuery("getOrLoad", new GetOrLoadCacheQuery());
    }

    @Override
    public DKCoinsUser getUser(String name) {
        MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(name);
        if(player != null) {
            return getUser(player.getUniqueId());
        }
        return null;
    }

    @Override
    public DKCoinsUser constructNewUser(UUID uniqueId) {
        return new MinecraftDKCoinsUser(uniqueId);
    }

    public static class GetOrLoadCacheQuery implements CacheQuery<DKCoinsUser> {


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
                return new MinecraftDKCoinsUser((UUID) identifiers[0]);
            }

    }
}
