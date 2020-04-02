package net.pretronic.dkcoins.minecraft.migration;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.minecraft.DKCoinsConfig;
import org.bukkit.Bukkit;

import java.util.UUID;

public class EssentialsXMigration implements Migration {

    @Override
    public String getName() {
        return "EssentialsX";
    }

    @Override
    public boolean migrate() {
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        for (UUID uniqueId : essentials.getUserMap().getAllUniqueUsers()) {
            User user = essentials.getUser(uniqueId);
            try {
                double balance = Economy.getMoneyExact(user.getName()).doubleValue();

                BankAccount account = DKCoins.getInstance().getStorage().createAccount(user.getName(),
                        DKCoins.getInstance().getAccountManager().searchAccountType("User"),
                        false, null, DKCoins.getInstance().getUserManager().getUser(uniqueId));

                account.getCredit(DKCoinsConfig.CURRENCY_DEFAULT).setAmount(balance);
            } catch (UserDoesNotExistException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
