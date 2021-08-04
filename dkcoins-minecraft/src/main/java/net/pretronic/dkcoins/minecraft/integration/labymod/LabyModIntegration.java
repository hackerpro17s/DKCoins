/*
 * (C) Copyright 2021 The DKBans Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 21.02.21, 08:35
 *
 * The DKBans Project is under the Apache License, version 2.0 (the "License");
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

package net.pretronic.dkcoins.minecraft.integration.labymod;

import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.libraries.utility.Convert;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.client.CustomClient;
import org.mcnative.runtime.api.player.client.LabyModClient;

public class LabyModIntegration {

    public static final LabyModClient.EnumBalanceType BALANCE_TYPE_BANK = LabyModClient.EnumBalanceType.BANK;
    public static final LabyModClient.EnumBalanceType BALANCE_TYPE_CASH = LabyModClient.EnumBalanceType.CASH;

    public static LabyModClient.EnumBalanceType getBalanceType(AccountType type) {
        if(type.getName().equalsIgnoreCase("user")) return BALANCE_TYPE_CASH;
        return BALANCE_TYPE_BANK;
    }

    public static void sendPlayerBalance(ConnectedMinecraftPlayer player, AccountCredit credit) {
        sendPlayerBalance(player, getBalanceType(credit.getAccount().getType()), credit.getAmount());
    }

    public static void sendPlayerBalance(ConnectedMinecraftPlayer player, LabyModClient.EnumBalanceType cashType, double balance) {
        if(player.isCustomClient(CustomClient.LABYMOD)) {
            player.getCustomClient(CustomClient.LABYMOD).updateBalanceDisplay(cashType, Convert.toInteger(balance));
        }

    }
}
