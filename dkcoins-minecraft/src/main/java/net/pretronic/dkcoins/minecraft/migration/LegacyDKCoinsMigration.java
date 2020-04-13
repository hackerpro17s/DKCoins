package net.pretronic.dkcoins.minecraft.migration;

import ch.dkrieger.coinsystem.core.DKCoinsLegacy;
import ch.dkrieger.coinsystem.core.player.CoinPlayer;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.migration.Migration;
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
    public Result migrate(Currency currency) {
        long start = System.currentTimeMillis();
        File location = new File("plugins/DKCoins/legacy-config.yml");
        if(!location.exists()) {
            DKCoins.getInstance().getLogger().error("No dkcoins legacy config was found");
            return new Result(false, 0, 0, 0, 0, -1);
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

        return new Result(true, totalCount.get(), dkcoinsCount.get(), mcNativeCount.get(), 0,System.currentTimeMillis()-start);
    }
}
