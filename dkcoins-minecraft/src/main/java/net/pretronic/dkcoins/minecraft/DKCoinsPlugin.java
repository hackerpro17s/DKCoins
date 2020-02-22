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
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.transaction.AccountTransactionProperty;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.minecraft.commands.BankObjectCommand;
import org.mcnative.common.McNative;
import org.mcnative.common.plugin.MinecraftPlugin;

import java.util.ArrayList;
import java.util.Collection;

public class DKCoinsPlugin extends MinecraftPlugin {

    private static DKCoinsPlugin INSTANCE;

    @Lifecycle(state = LifecycleState.LOAD)
    public void onBootstrap(LifecycleState state) {
        System.out.println("START");
        INSTANCE = this;
        //@Todo change for service and proxy
        TransactionPropertyBuilder builder = member -> new ArrayList<>();
        new MinecraftDKCoins(builder);
        McNative.getInstance().getLocal().getCommandManager().registerCommand(new BankObjectCommand(this));
    }

    public static DKCoinsPlugin getInstance() {
        return INSTANCE;
    }
}