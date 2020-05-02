package net.pretronic.dkcoins.minecraft.migration;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.utility.io.FileUtil;
import org.mcnative.common.McNative;
import org.mcnative.common.player.data.PlayerDataProvider;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class EssentialsXMigration implements Migration {

    @Override
    public String getName() {
        return "EssentialsX";
    }

    @Override
    public Result migrate(Currency currency) {
        long start = System.currentTimeMillis();

        File folderName = new File("plugins/Essentials/userdata/");

        if(!folderName.exists()) {
            DKCoins.getInstance().getLogger().error("Old Essentials data doesn't exist");
            return new Result(false, 0, 0, 0, 0, -1);
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

                if(user.getName() == null) {
                    DKCoins.getInstance().getLogger().warn("Skipped migration for user with uuid [{}] and name [{}]", uniqueId, name);
                    skipped.incrementAndGet();
                } else {
                    if(DKCoins.getInstance().getAccountManager().getAccount(user.getName(), "User") == null) {
                        BankAccount account = DKCoins.getInstance().getStorage().createAccount(user.getName(),
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
        return new Result(true, totalCount.get(), dkcoinsCount.get(), mcNativeCount.get(),
                skipped.get(), System.currentTimeMillis()-start);
    }
}
