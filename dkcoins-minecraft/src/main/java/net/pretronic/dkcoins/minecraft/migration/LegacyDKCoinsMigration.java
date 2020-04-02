package net.pretronic.dkcoins.minecraft.migration;

import ch.dkrieger.coinsystem.core.DKCoinsLegacy;
import ch.dkrieger.coinsystem.core.player.CoinPlayer;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import org.mcnative.common.McNative;
import org.mcnative.common.player.data.PlayerDataProvider;

public class LegacyDKCoinsMigration implements Migration {

    @Override
    public String getName() {
        return "DKCoins";
    }

    @Override
    public boolean migrate() {
        if(DKCoinsLegacy.getInstance() == null) {
            DKCoinsLegacy.setInstance(new DKCoinsLegacy());
        }
        PlayerDataProvider playerDataProvider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
        for (CoinPlayer player : DKCoinsLegacy.getInstance().getStorage().getPlayers()) {
            BankAccount account = DKCoins.getInstance().getStorage().createAccount(player.getName(),
                    DKCoins.getInstance().getAccountManager().searchAccountType("User"),
                    false, null, DKCoins.getInstance().getUserManager().getUser(player.getUUID()));
            account.getCredit(DKCoinsConfig.CURRENCY_DEFAULT).setAmount(player.getCoins());
            if(McNative.getInstance().getPlayerManager().getPlayer(player.getUUID()) == null) {
                playerDataProvider.createPlayerData(player.getName(), player.getUUID(), -1, player.getFirstLogin()
                        , player.getLastLogin(), null);
            }
        }
        return true;
    }
}
