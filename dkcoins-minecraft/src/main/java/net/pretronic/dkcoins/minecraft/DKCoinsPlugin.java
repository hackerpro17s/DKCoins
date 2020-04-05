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

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.level.LogLevel;
import net.pretronic.libraries.plugin.lifecycle.Lifecycle;
import net.pretronic.libraries.plugin.lifecycle.LifecycleState;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.minecraft.commands.bank.BankCommand;
import net.pretronic.dkcoins.minecraft.commands.currency.CurrencyCommand;
import net.pretronic.dkcoins.minecraft.listener.MinecraftPlayerListener;
import net.pretronic.libraries.utility.io.FileUtil;
import org.mcnative.common.McNative;
import org.mcnative.common.plugin.MinecraftPlugin;

import java.io.File;
import java.util.ArrayList;

public class DKCoinsPlugin extends MinecraftPlugin {

    private static DKCoinsPlugin INSTANCE;

    @Lifecycle(state = LifecycleState.LOAD)
    public void onBootstrap(LifecycleState state) {
        getLogger().setLevel(LogLevel.ALL);
        INSTANCE = this;

        loadConfig();

        //@Todo change for service and proxy
        TransactionPropertyBuilder builder = member -> new ArrayList<>();
        new MinecraftDKCoins(getLogger(), builder);

        setUpdateConfiguration(DKCoinsConfig.AUTO_UPDATE_ENABLED,DKCoinsConfig.AUTO_UPDATE_QUALIFIER);
    }

    private void loadConfig() {
        File configLocation = new File("plugins/DKCoins/config.yml");

        if(configLocation.exists()) {
            Document oldConfig = DocumentFileType.YAML.getReader().read(configLocation);

            if(oldConfig.contains("storage.mongodb")) {
                getLogger().info("DKCoins Legacy detected");

                File legacyConfigLocation = new File("plugins/DKCoins/legacy-config.yml");
                FileUtil.copyFile(configLocation, legacyConfigLocation);

                boolean success = configLocation.delete();
                if(success) {
                    getLogger().info("DKCoins Legacy config successful copied to legacy-config.yml");
                } else {
                    getLogger().error("DKCoins Legacy config can't be copied to legacy-config.yml");
                }
            }
        }
        getConfiguration().load(DKCoinsConfig.class);
        getLogger().info("DKCoins config loaded");
    }


    public static DKCoinsPlugin getInstance() {
        return INSTANCE;
    }
}
