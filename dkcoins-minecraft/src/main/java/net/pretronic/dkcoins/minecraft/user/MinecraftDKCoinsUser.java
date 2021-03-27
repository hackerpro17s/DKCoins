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

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.common.user.DefaultDKCoinsUser;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.MinecraftPlayer;

import java.util.UUID;

public class MinecraftDKCoinsUser extends DefaultDKCoinsUser {

    public MinecraftDKCoinsUser(UUID uniqueId) {
        super(uniqueId);
    }

    @Override
    public String getName() {
        MinecraftPlayer player = getAsPlayer();
        if(player == null) {
            DKCoins.getInstance().getLogger().warn("Player with uuid [{}] is not in McNative registered", getUniqueId());
            return null;
        }
        return player.getName();
    }

    @Override
    public String getDisplayName() {
        MinecraftPlayer player = getAsPlayer();
        if(player == null) {
            DKCoins.getInstance().getLogger().warn("Player with uuid [{}] is not in McNative registered", getUniqueId());
            return null;
        }
        return player.getDisplayName();
    }

    private MinecraftPlayer getAsPlayer() {
        return McNative.getInstance().getPlayerManager().getPlayer(getUniqueId());
    }

}
