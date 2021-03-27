package net.pretronic.dkcoins.minecraft.integration.labymod;

import net.pretronic.dkcoins.api.account.AccountCredit;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.events.account.DKCoinsAccountTransactEvent;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.DKCoinsPlugin;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.event.Listener;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerPostLoginEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;

import java.util.concurrent.TimeUnit;

public class LabyModServiceListener {

    @Listener(priority = 100)
    public void onPlayerLogin(MinecraftPlayerPostLoginEvent event){
        McNative.getInstance().getScheduler().createTask(DKCoinsPlugin.getInstance())
                .delay(1, TimeUnit.SECONDS)
                .execute(()-> {
                    DKCoinsUser user = event.getOnlinePlayer().getAs(DKCoinsUser.class);

                    LabyModIntegration.sendPlayerBalance(event.getPlayer().getAsConnectedPlayer(), user.getDefaultAccount().getCredit(DKCoinsConfig.LABYMOD_BALANCE_CASH_CURRENCY));

                    for (BankAccount account : user.getAccounts()) {
                        if(account.getType().getName().equalsIgnoreCase("user")) continue;
                        LabyModIntegration.sendPlayerBalance(event.getPlayer().getAsConnectedPlayer(), account.getCredit(DKCoinsConfig.LABYMOD_BALANCE_BANK_CURRENCY));
                        break;
                    }
                });
    }

    @Listener
    public void onTransact(DKCoinsAccountTransactEvent event) {
        Currency currency = event.getTransaction().getCurrency();
        if(currency.equals(DKCoinsConfig.LABYMOD_BALANCE_BANK_CURRENCY) && DKCoinsConfig.LABYMOD_BALANCE_BANK_ENABLED) {
            sendCurrencyUpdate(event.getTransaction().getSource());
            sendCurrencyUpdate(event.getTransaction().getReceiver());
        }

        if(currency.equals(DKCoinsConfig.LABYMOD_BALANCE_CASH_CURRENCY) && DKCoinsConfig.LABYMOD_BALANCE_CASH_ENABLED) {
            sendCurrencyUpdate(event.getTransaction().getSource());
            sendCurrencyUpdate(event.getTransaction().getReceiver());
        }
    }

    private void sendCurrencyUpdate(AccountCredit credit) {
        for (AccountMember member : credit.getAccount().getMembers()) {
            ConnectedMinecraftPlayer connectedMinecraftPlayer = McNative.getInstance().getLocal().getConnectedPlayer(member.getUser().getUniqueId());
            if(connectedMinecraftPlayer != null) {
                LabyModIntegration.sendPlayerBalance(connectedMinecraftPlayer, credit);
            }
        }
    }
}