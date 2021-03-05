package net.pretronic.dkcoins.minecraft.migration;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.api.migration.MigrationResult;
import net.pretronic.dkcoins.api.migration.MigrationResultBuilder;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.entry.DocumentEntry;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.utility.Convert;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.player.profile.GameProfile;
import org.mcnative.runtime.api.player.profile.GameProfileLoader;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenManagerYMLMigration implements Migration {

    @Override
    public String getName() {
        return "TokenManager-YML";
    }

    @Override
    public MigrationResult migrate(Currency currency) {
        File dataLocation = new File("plugins/TokenManager/data.yml");
        if(dataLocation.exists()) {
            PlayerDataProvider playerDataProvider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
            GameProfileLoader gameProfileLoader = McNative.getInstance().getRegistry().getService(GameProfileLoader.class);

            long start = System.currentTimeMillis();
            AtomicInteger totalCount = new AtomicInteger();
            AtomicInteger mcNativeCount = new AtomicInteger();
            AtomicInteger dkcoinsCount = new AtomicInteger();
            AtomicInteger skipped = new AtomicInteger();

            Document data = DocumentFileType.YAML.getReader().read(dataLocation);
            for (DocumentEntry player : data.getDocument("Players")) {
                String identifier = player.getKey();
                try {
                    UUID playerId = Convert.toUUID(identifier);
                    String name = null;
                    int balance = player.toPrimitive().getAsInt();

                    if(McNative.getInstance().getPlayerManager().getPlayer(playerId) == null) {
                        System.out.println(playerId);
                        GameProfile profile = gameProfileLoader.getGameProfile(playerId);
                        if(profile == null) {
                            skipped.incrementAndGet();
                            continue;
                        }
                        name = profile.getName();
                        playerDataProvider.createPlayerData(profile.getName(), playerId, -1, -1, -1, null);
                        mcNativeCount.incrementAndGet();
                    }

                    DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(playerId);

                    if(user != null) {
                        if(DKCoins.getInstance().getAccountManager().getAccount(name, "User") == null) {
                            BankAccount account = DKCoins.getInstance().getAccountManager().createAccount(name,
                                    DKCoins.getInstance().getAccountManager().searchAccountType("User"),
                                    false, null, DKCoins.getInstance().getUserManager().getUser(playerId));
                            account.getCredit(currency).setAmount(balance);
                            dkcoinsCount.incrementAndGet();
                        }

                        totalCount.getAndIncrement();
                    } else {
                        DKCoins.getInstance().getLogger().warn("Skipped migration for user with uuid [{}]", player);
                        skipped.incrementAndGet();
                    }
                } catch (IllegalArgumentException ignored) {
                    skipped.incrementAndGet();
                }
            }
            return new MigrationResultBuilder()
                    .setSuccess(true)
                    .setTotalMigrateCount(totalCount.get())
                    .setDkcoinsAccountMigrateCount(dkcoinsCount.get())
                    .setMcNativeMigrateCount(mcNativeCount.get())
                    .setSkipped(skipped.get())
                    .setTime(System.currentTimeMillis() - start)
                    .build();
        }
        return new MigrationResultBuilder()
                .setSuccess(false)
                .build();
    }
}
