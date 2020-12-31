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

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.minecraft.commands.bank.BankCommand;
import net.pretronic.dkcoins.minecraft.commands.bank.BankTransferCommand;
import net.pretronic.dkcoins.minecraft.commands.currency.CurrencyCommand;
import net.pretronic.dkcoins.minecraft.commands.dkcoins.DKCoinsCommand;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.dkcoins.minecraft.listener.InternalListener;
import net.pretronic.dkcoins.minecraft.listener.MinecraftPlayerListener;
import net.pretronic.dkcoins.minecraft.migration.EssentialsXMigration;
import net.pretronic.dkcoins.minecraft.migration.LegacyDKCoinsMigration;
import net.pretronic.dkcoins.minecraft.user.MinecraftDKCoinsUser;
import net.pretronic.dkcoins.minecraft.user.MinecraftDKCoinsUserManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.level.LogLevel;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriberRegistry;
import net.pretronic.libraries.plugin.lifecycle.Lifecycle;
import net.pretronic.libraries.plugin.lifecycle.LifecycleState;
import net.pretronic.libraries.synchronisation.UnconnectedSynchronisationCaller;
import net.pretronic.libraries.utility.io.FileUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.plugin.MinecraftPlugin;
import org.mcnative.runtime.api.plugin.configuration.ConfigurationProvider;
import org.mcnative.runtime.api.serviceprovider.economy.EconomyProvider;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderHelper;

import java.io.File;
import java.util.ArrayList;

public class DKCoinsPlugin extends MinecraftPlugin {

    private static DKCoinsPlugin INSTANCE;

    @Lifecycle(state = LifecycleState.LOAD)
    public void onBootstrap(LifecycleState state) {
        getLogger().setLevel(LogLevel.ALL);
        INSTANCE = this;

        loadConfig();
        VariableDescriberRegistry.registerDescriber(MinecraftDKCoinsUser.class);

        registerEconomyProvider();
        PlaceholderHelper.registerPlaceHolders(DKCoinsPlugin.getInstance(), "dkcoins", new DKCoinsPlaceholderHook());

        //@Todo change for service and proxy
        TransactionPropertyBuilder builder = member -> new ArrayList<>();
        DefaultDKCoins dkCoins = new DefaultDKCoins(getLogger(),
                McNative.getInstance().getLocal().getEventBus(),
                McNative.getInstance().getPluginManager().getService(ConfigurationProvider.class).getDatabase(DKCoinsPlugin.getInstance(), true),
                new MinecraftDKCoinsUserManager(),
                builder,
                new MinecraftDKCoinsFormatter());

        registerCommandsAndListeners();
        registerPlayerAdapter();

        initAccountManager(dkCoins);
        initCurrencyManager(dkCoins);
        setupMigration(dkCoins);
        DKCoinsConfig.init();
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

    private void registerPlayerAdapter() {
        McNative.getInstance().getPlayerManager().registerPlayerAdapter(DKCoinsUser.class, minecraftPlayer ->
                DKCoins.getInstance().getUserManager().getUser(minecraftPlayer.getUniqueId()));
    }

    private void registerCommandsAndListeners() {
        McNative.getInstance().getLocal().getCommandManager().registerCommand(new DKCoinsCommand(DKCoinsPlugin.getInstance()));
        McNative.getInstance().getLocal().getCommandManager().registerCommand(new BankCommand(DKCoinsPlugin.getInstance()));
        McNative.getInstance().getLocal().getCommandManager().registerCommand(new CurrencyCommand(DKCoinsPlugin.getInstance()));
        McNative.getInstance().getLocal().getCommandManager().registerCommand(new BankTransferCommand(DKCoinsPlugin.getInstance(), DKCoinsConfig.COMMAND_PAY,Messages.COMMAND_USER_BANK_TRANSFER_HELP));


        DKCoins.getInstance().getEventBus().subscribe(getInstance(), new MinecraftPlayerListener());
        DKCoins.getInstance().getEventBus().subscribe(getInstance(), new InternalListener());
    }

    private void registerEconomyProvider() {
        if(DKCoinsConfig.ECONOMY_PROVIDER_ENABLED) {
            getLogger().info("Enabling economy provider");
            McNative.getInstance().getPluginManager().registerService(DKCoinsPlugin.getInstance(), EconomyProvider.class,
                    new DKCoinsEconomyProvider(), DKCoinsConfig.ECONOMY_PROVIDER_PRIORITY);
            getLogger().info("Economy provider enabled with priority " + DKCoinsConfig.ECONOMY_PROVIDER_PRIORITY);
        }
    }

    private void setupMigration(DKCoins dkCoins) {
        dkCoins.registerMigration(new LegacyDKCoinsMigration());
        dkCoins.registerMigration(new EssentialsXMigration());
    }

    private void initCurrencyManager(DefaultDKCoins dkCoins) {
        if(McNative.getInstance().isNetworkAvailable()) {
            McNative.getInstance().getNetwork().getMessenger().registerSynchronizingChannel("dkcoins_currency", DKCoinsPlugin.getInstance(),
                    int.class, dkCoins.getCurrencyManager());
            McNative.getInstance().getNetwork().registerStatusCallback(DKCoinsPlugin.getInstance(), dkCoins.getCurrencyManager());
        } else {
            dkCoins.getCurrencyManager().init(new UnconnectedSynchronisationCaller<>(true));
        }
        dkCoins.getCurrencyManager().getLoadedCurrencies().addAll(DKCoins.getInstance().getStorage().getCurrencies());

        if(dkCoins.getCurrencyManager().getLoadedCurrencies().isEmpty() && dkCoins.getCurrencyManager().searchCurrency("Coins") == null) {
            dkCoins.getCurrencyManager().createCurrency("Coins", "$");
        }
    }

    private void initAccountManager(DefaultDKCoins dkCoins) {
        if(McNative.getInstance().isNetworkAvailable()) {
            McNative.getInstance().getNetwork().getMessenger().registerSynchronizingChannel("dkcoins_accountType", DKCoinsPlugin.getInstance(),
                    int.class, DefaultDKCoins.getInstance().getAccountManager().getAccountTypeCache());
            McNative.getInstance().getNetwork().registerStatusCallback(DKCoinsPlugin.getInstance(), DefaultDKCoins.getInstance().getAccountManager().getAccountTypeCache());
        } else {
            DefaultDKCoins.getInstance().getAccountManager().getAccountTypeCache().initUnconnected();
        }


        if(McNative.getInstance().isNetworkAvailable()) {
            McNative.getInstance().getNetwork().getMessenger().registerSynchronizingChannel("dkcoins_account", DKCoinsPlugin.getInstance(),
                    int.class, DefaultDKCoins.getInstance().getAccountManager().getAccountCache());
            McNative.getInstance().getNetwork().registerStatusCallback(DKCoinsPlugin.getInstance(), DefaultDKCoins.getInstance().getAccountManager().getAccountCache());
        } else {
            DefaultDKCoins.getInstance().getAccountManager().getAccountCache().initUnconnected();
        }

        dkCoins.createDefaults();
    }

    public static DKCoinsPlugin getInstance() {
        return INSTANCE;
    }
}
