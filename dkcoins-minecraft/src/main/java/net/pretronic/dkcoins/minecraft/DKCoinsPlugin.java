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

import net.prematic.libraries.plugin.lifecycle.Lifecycle;
import net.prematic.libraries.plugin.lifecycle.LifecycleState;
import org.mcnative.common.plugin.MinecraftPlugin;

public class DKCoinsPlugin extends MinecraftPlugin {

    static DKCoinsPlugin INSTANCE;

    @Lifecycle(state = LifecycleState.LOAD)
    public void onBootstrap(LifecycleState state) {
        System.out.println("START");
        INSTANCE = this;
        new MinecraftDKCoins();
    }
}