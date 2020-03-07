/*
 * (C) Copyright 2019 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 18.11.19, 21:12
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft;

import net.prematic.libraries.logging.level.LogLevel;
import net.prematic.libraries.plugin.lifecycle.Lifecycle;
import net.prematic.libraries.plugin.lifecycle.LifecycleState;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.minecraft.commands.bank.BankCommand;
import net.pretronic.dkcoins.minecraft.commands.currency.CurrencyCommand;
import net.pretronic.dkcoins.minecraft.listener.MinecraftPlayerListener;
import org.mcnative.common.McNative;
import org.mcnative.common.plugin.MinecraftPlugin;

import java.util.ArrayList;

public class DKCoinsPlugin extends MinecraftPlugin {

    private static DKCoinsPlugin INSTANCE;

    @Lifecycle(state = LifecycleState.LOAD)
    public void onBootstrap(LifecycleState state) {
        getLogger().setLevel(LogLevel.ALL);
        INSTANCE = this;

        getConfiguration().load(DKCoinsConfig.class);


        //@Todo change for service and proxy
        TransactionPropertyBuilder builder = member -> new ArrayList<>();
        new MinecraftDKCoins(builder);
        McNative.getInstance().getLocal().getCommandManager().registerCommand(new BankCommand(this));
        McNative.getInstance().getLocal().getCommandManager().registerCommand(new CurrencyCommand(this));
        McNative.getInstance().getLocal().getEventBus().subscribe(this, new MinecraftPlayerListener());
    }

    public static DKCoinsPlugin getInstance() {
        return INSTANCE;
    }
}