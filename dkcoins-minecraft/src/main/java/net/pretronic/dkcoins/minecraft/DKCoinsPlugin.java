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
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.transaction.TransactionPropertyBuilder;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.common.DefaultDKCoins;
import net.pretronic.dkcoins.common.account.DefaultAccountCredit;
import net.pretronic.dkcoins.common.account.DefaultAccountLimitation;
import net.pretronic.dkcoins.common.account.DefaultBankAccount;
import net.pretronic.dkcoins.common.account.DefaultRankedAccountCredit;
import net.pretronic.dkcoins.common.account.member.DefaultAccountMember;
import net.pretronic.dkcoins.common.account.member.DefaultAccountMemberRole;
import net.pretronic.dkcoins.common.account.transaction.DefaultAccountTransaction;
import net.pretronic.dkcoins.common.currency.DefaultCurrency;
import net.pretronic.dkcoins.common.currency.DefaultCurrencyExchangeRate;
import net.pretronic.dkcoins.minecraft.commands.bank.BankCommand;
import net.pretronic.dkcoins.minecraft.commands.bank.BankTransferCommand;
import net.pretronic.dkcoins.minecraft.commands.currency.CurrencyCommand;
import net.pretronic.dkcoins.minecraft.commands.dkcoins.DKCoinsCommand;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.dkcoins.minecraft.listener.InternalListener;
import net.pretronic.dkcoins.minecraft.listener.MinecraftPlayerListener;
import net.pretronic.dkcoins.minecraft.migration.EssentialsXMigration;
import net.pretronic.dkcoins.minecraft.migration.LegacyDKCoinsMigration;
import net.pretronic.dkcoins.minecraft.migration.TokenManagerMySQLMigration;
import net.pretronic.dkcoins.minecraft.migration.TokenManagerYMLMigration;
import net.pretronic.dkcoins.minecraft.user.MinecraftDKCoinsUser;
import net.pretronic.dkcoins.minecraft.user.MinecraftDKCoinsUserManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.level.LogLevel;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriber;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriberRegistry;
import net.pretronic.libraries.plugin.lifecycle.Lifecycle;
import net.pretronic.libraries.plugin.lifecycle.LifecycleState;
import net.pretronic.libraries.synchronisation.UnconnectedSynchronisationCaller;
import net.pretronic.libraries.utility.io.FileUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.plugin.MinecraftPlugin;
import org.mcnative.runtime.api.serviceprovider.economy.EconomyProvider;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderHelper;
import org.mcnative.runtime.api.text.format.ColoredString;

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
        registerVariableDescribers();
        PlaceholderHelper.registerPlaceHolders(DKCoinsPlugin.getInstance(), "dkcoins", new DKCoinsPlaceholderHook());

        TransactionPropertyBuilder builder = member -> new ArrayList<>();
        DefaultDKCoins dkCoins = new DefaultDKCoins(getLogger(),
                McNative.getInstance().getLocal().getEventBus(),
                getDatabaseOrCreate(),
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
        McNative.getInstance().getPlayerManager().registerPlayerAdapter(DKCoinsUser.class, minecraftPlayer
                -> DKCoins.getInstance().getUserManager().getUser(minecraftPlayer.getUniqueId()));
    }

    private void registerCommandsAndListeners() {
        getRuntime().getLocal().getCommandManager().registerCommand(new DKCoinsCommand(DKCoinsPlugin.getInstance()));
        getRuntime().getLocal().getCommandManager().registerCommand(new BankCommand(DKCoinsPlugin.getInstance()));
        getRuntime().getLocal().getCommandManager().registerCommand(new CurrencyCommand(DKCoinsPlugin.getInstance()));
        getRuntime().getLocal().getCommandManager().registerCommand(new BankTransferCommand(DKCoinsPlugin.getInstance(), DKCoinsConfig.COMMAND_PAY,Messages.COMMAND_USER_BANK_TRANSFER_HELP));

        getRuntime().getLocal().getEventBus().subscribe(getInstance(), new MinecraftPlayerListener());
        getRuntime().getLocal().getEventBus().subscribe(getInstance(), new InternalListener());
    }

    private void registerEconomyProvider() {
        if(DKCoinsConfig.ECONOMY_PROVIDER_ENABLED) {
            getLogger().info("Enabling economy provider");
            getRuntime().getPluginManager().registerService(DKCoinsPlugin.getInstance(), EconomyProvider.class,
                    new DKCoinsEconomyProvider(), DKCoinsConfig.ECONOMY_PROVIDER_PRIORITY);
            getLogger().info("Economy provider enabled with priority " + DKCoinsConfig.ECONOMY_PROVIDER_PRIORITY);
        }
    }

    private void registerVariableDescribers() {
        VariableDescriberRegistry.registerDescriber(DefaultCurrency.class);
        VariableDescriberRegistry.registerDescriber(DefaultCurrencyExchangeRate.class);
        VariableDescriberRegistry.registerDescriber(DefaultAccountMember.class);
        VariableDescriberRegistry.registerDescriber(DefaultAccountTransaction.class);

        VariableDescriber<DefaultAccountMemberRole> roleVariableDescriber = VariableDescriberRegistry.registerDescriber(DefaultAccountMemberRole.class);
        roleVariableDescriber.registerFunction("parentRoleName", role -> role.getParentRole() == null ? "none" : role.getParentRole().getName());

        VariableDescriberRegistry.registerDescriber(DefaultAccountCredit.class);
        VariableDescriberRegistry.registerDescriber(DefaultAccountLimitation.class);
        VariableDescriberRegistry.registerDescriber(DefaultRankedAccountCredit.class);

        VariableDescriber<DefaultBankAccount> describer = VariableDescriberRegistry.registerDescriber(DefaultBankAccount.class);
        ColoredString.makeFunctionColored(describer,"displayName");

        VariableDescriberRegistry.registerDescriber(AccessRight.class);
    }


    private void setupMigration(DKCoins dkCoins) {
        dkCoins.registerMigration(new LegacyDKCoinsMigration());
        dkCoins.registerMigration(new EssentialsXMigration());
        dkCoins.registerMigration(new TokenManagerYMLMigration());
        dkCoins.registerMigration(new TokenManagerMySQLMigration());
    }

    private void initCurrencyManager(DefaultDKCoins dkCoins) {
        if(getRuntime().isNetworkAvailable()) {
            getRuntime().getNetwork().getMessenger().registerSynchronizingChannel("dkcoins_currency"
                    , DKCoinsPlugin.getInstance(), int.class, dkCoins.getCurrencyManager());
            getRuntime().getNetwork().registerStatusCallback(DKCoinsPlugin.getInstance(), dkCoins.getCurrencyManager());
        } else {
            dkCoins.getCurrencyManager().init(new UnconnectedSynchronisationCaller<>(true));
        }

        DefaultDKCoins.getInstance().getStorage().getCurrency().find().execute()
                .loadIn(dkCoins.getCurrencyManager().getLoadedCurrencies(), entry -> new DefaultCurrency(
                        entry.getInt("Id"),
                        entry.getString("Name"),
                        entry.getString("Symbol")));

        if(dkCoins.getCurrencyManager().getLoadedCurrencies().isEmpty() && dkCoins.getCurrencyManager().searchCurrency("Coins") == null) {
            dkCoins.getCurrencyManager().createCurrency("Coins", "$");
        }
    }

    private void initAccountManager(DefaultDKCoins dkCoins) {
        if(getRuntime().isNetworkAvailable()) {
            getRuntime().getNetwork().getMessenger().registerSynchronizingChannel("dkcoins_accountType", DKCoinsPlugin.getInstance(),
                    int.class, DefaultDKCoins.getInstance().getAccountManager().getAccountTypeCache());
            getRuntime().getNetwork().registerStatusCallback(DKCoinsPlugin.getInstance(), DefaultDKCoins.getInstance().getAccountManager().getAccountTypeCache());
        } else {
            DefaultDKCoins.getInstance().getAccountManager().getAccountTypeCache().initUnconnected();
        }

        if(McNative.getInstance().isNetworkAvailable()) {
            getRuntime().getNetwork().getMessenger().registerSynchronizingChannel("dkcoins_account", DKCoinsPlugin.getInstance(),
                    int.class, DefaultDKCoins.getInstance().getAccountManager().getAccountCache());
            getRuntime().getNetwork().registerStatusCallback(DKCoinsPlugin.getInstance(), DefaultDKCoins.getInstance().getAccountManager().getAccountCache());
        } else {
            DefaultDKCoins.getInstance().getAccountManager().getAccountCache().initUnconnected();
        }

        dkCoins.createDefaults();
    }

    public static DKCoinsPlugin getInstance() {
        return INSTANCE;
    }
}
