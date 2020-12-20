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

package net.pretronic.dkcoins.minecraft.migration;

import ch.dkrieger.coinsystem.core.DKCoinsLegacy;
import ch.dkrieger.coinsystem.core.player.CoinPlayer;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.api.migration.MigrationResult;
import org.mcnative.common.McNative;
import org.mcnative.common.player.data.PlayerDataProvider;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class LegacyDKCoinsMigration implements Migration {

    @Override
    public String getName() {
        return "DKCoinsLegacy";
    }

    @Override
    public MigrationResult migrate(Currency currency) {
        long start = System.currentTimeMillis();
        File location = new File("plugins/DKCoins/legacy-config.yml");
        if(!location.exists()) {
            DKCoins.getInstance().getLogger().error("No dkcoins legacy config was found");
            return new MigrationResult(false, 0, 0, 0, 0, -1);
        }
        if(DKCoinsLegacy.getInstance() == null) {
            DKCoinsLegacy.setInstance(new DKCoinsLegacy());
        }
        PlayerDataProvider playerDataProvider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
        AtomicInteger totalCount = new AtomicInteger();
        AtomicInteger mcNativeCount = new AtomicInteger();
        AtomicInteger dkcoinsCount = new AtomicInteger();

        for (CoinPlayer player : DKCoinsLegacy.getInstance().getStorage().getPlayers()) {

            if(DKCoins.getInstance().getAccountManager().getAccount(player.getName(), "User") == null) {
                BankAccount account = DKCoins.getInstance().getStorage().createAccount(player.getName(),
                        DKCoins.getInstance().getAccountManager().searchAccountType("User"),
                        false, null, DKCoins.getInstance().getUserManager().getUser(player.getUUID()));
                account.getCredit(currency).setAmount(player.getCoins());
                dkcoinsCount.incrementAndGet();
            }

            if(McNative.getInstance().getPlayerManager().getPlayer(player.getUUID()) == null
                    && McNative.getInstance().getPlayerManager().getPlayer(player.getName()) == null) {
                playerDataProvider.createPlayerData(player.getName(), player.getUUID(), -1, player.getFirstLogin()
                        , player.getLastLogin(), null);
                mcNativeCount.incrementAndGet();
            }
            totalCount.incrementAndGet();
        }

        return new MigrationResult(true, totalCount.get(), dkcoinsCount.get(), mcNativeCount.get(), 0,System.currentTimeMillis()-start);
    }
}
