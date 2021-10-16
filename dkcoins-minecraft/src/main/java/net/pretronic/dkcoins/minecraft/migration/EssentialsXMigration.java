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

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.api.migration.MigrationResult;
import net.pretronic.dkcoins.api.migration.MigrationResultBuilder;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.utility.io.FileUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class EssentialsXMigration implements Migration {

    @Override
    public String getName() {
        return "EssentialsX";
    }

    @Override
    public MigrationResult migrate(Currency currency) {
        long start = System.currentTimeMillis();

        File folderName = new File("plugins/Essentials/userdata/");

        if(!folderName.exists()) {
            DKCoins.getInstance().getLogger().error("Old Essentials data doesn't exist");
            return new MigrationResultBuilder().setSuccess(false).build();
        }

        PlayerDataProvider playerDataProvider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);

        AtomicInteger totalCount = new AtomicInteger();
        AtomicInteger mcNativeCount = new AtomicInteger();
        AtomicInteger dkcoinsCount = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        FileUtil.processFilesHierarchically(folderName, file -> {
            Document document = DocumentFileType.YAML.getReader().read(file);
            UUID uniqueId = UUID.fromString(file.getName().split("\\.")[0]);
            String name = document.getString("lastAccountName");
            double balance = document.getDouble("money");
            long firstLogin = document.getLong("timestamps.login");
            long lastLogin = document.getLong("timestamps.logout");

            if(name != null) {
                if(McNative.getInstance().getPlayerManager().getPlayer(name) == null
                        && McNative.getInstance().getPlayerManager().getPlayer(uniqueId) == null) {
                    playerDataProvider.createPlayerData(name, uniqueId, -1, firstLogin, lastLogin, null);
                    mcNativeCount.incrementAndGet();
                }

                DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(uniqueId);

                if(user == null || user.getName() == null) {
                    DKCoins.getInstance().getLogger().warn("Skipped migration for user with uuid [{}] and name [{}]", uniqueId, name);
                    skipped.incrementAndGet();
                } else {
                    if(DKCoins.getInstance().getAccountManager().getAccount(user.getName(), "User") == null) {
                        BankAccount account = DKCoins.getInstance().getAccountManager().createAccount(user.getName(),
                                DKCoins.getInstance().getAccountManager().searchAccountType("User"),
                                false, null, DKCoins.getInstance().getUserManager().getUser(uniqueId));
                        account.getCredit(currency).setAmount(balance);
                        dkcoinsCount.incrementAndGet();
                    }

                    totalCount.getAndIncrement();
                }
            } else {
                skipped.incrementAndGet();
            }
        });
        return new MigrationResultBuilder()
                .setSuccess(true)
                .setTotalMigrateCount(totalCount.get())
                .setDkcoinsAccountMigrateCount(dkcoinsCount.get())
                .setMcNativeMigrateCount(mcNativeCount.get())
                .setSkipped(skipped.get())
                .setTime(System.currentTimeMillis() - start)
                .build();
    }
}
