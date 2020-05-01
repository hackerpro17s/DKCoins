package net.pretronic.dkcoins.minecraft.listener;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.user.DefaultDKCoinsUser;
import net.pretronic.libraries.event.Listener;
import org.mcnative.common.event.player.login.MinecraftPlayerLoginEvent;
import org.mcnative.common.player.OnlineMinecraftPlayer;

public class MinecraftPlayerListener {

    @Listener
    public void onPlayerLogin(MinecraftPlayerLoginEvent event) {
        OnlineMinecraftPlayer player = event.getOnlinePlayer();

        DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(player.getUniqueId());
        ((DefaultDKCoinsUser)user).initAccount();
    }
}
