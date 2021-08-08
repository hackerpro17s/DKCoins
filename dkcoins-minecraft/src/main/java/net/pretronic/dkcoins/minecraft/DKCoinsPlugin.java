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
import net.pretronic.dkcoins.common.user.DefaultDKCoinsUser;
import net.pretronic.dkcoins.common.user.DefaultDKCoinsUserManager;
import net.pretronic.dkcoins.minecraft.commands.bank.BankCommand;
import net.pretronic.dkcoins.minecraft.commands.bank.BankTransferCommand;
import net.pretronic.dkcoins.minecraft.commands.currency.CurrencyCommand;
import net.pretronic.dkcoins.minecraft.commands.dkcoins.DKCoinsCommand;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.dkcoins.minecraft.listener.InternalListener;
import net.pretronic.dkcoins.minecraft.listener.MinecraftPlayerListener;
import net.pretronic.dkcoins.minecraft.migration.EssentialsXMigration;
import net.pretronic.dkcoins.minecraft.migration.TokenManagerMySQLMigration;
import net.pretronic.dkcoins.minecraft.migration.TokenManagerYMLMigration;
import net.pretronic.libraries.caching.Cache;
import net.pretronic.libraries.caching.CacheQuery;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.level.LogLevel;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriber;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriberRegistry;
import net.pretronic.libraries.plugin.lifecycle.Lifecycle;
import net.pretronic.libraries.plugin.lifecycle.LifecycleState;
import net.pretronic.libraries.synchronisation.UnconnectedSynchronisationCaller;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.io.FileUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.NetworkIdentifier;
import org.mcnative.runtime.api.network.messaging.Messenger;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.plugin.MinecraftPlugin;
import org.mcnative.runtime.api.serviceprovider.economy.EconomyProvider;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderHelper;
import org.mcnative.runtime.api.text.format.ColoredString;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class DKCoinsPlugin extends MinecraftPlugin {

    private static DKCoinsPlugin INSTANCE;

    @Lifecycle(state = LifecycleState.LOAD)
    public void onBootstrap(LifecycleState state) {
        getLogger().setLevel(LogLevel.ALL);
        INSTANCE = this;

        loadConfig();
        VariableDescriberRegistry.registerDescriber(DefaultDKCoinsUser.class);
        registerEconomyProvider();
        registerVariableDescribers();
        PlaceholderHelper.registerPlaceHolders(DKCoinsPlugin.getInstance(), "dkcoins", new DKCoinsPlaceholderHook());

        TransactionPropertyBuilder builder = member -> new ArrayList<>();
        DefaultDKCoins dkCoins = new DefaultDKCoins(getLogger(),
                McNative.getInstance().getLocal().getEventBus(),
                getDatabaseOrCreate(),
                new DefaultDKCoinsUserManager(),
                builder,
                new MinecraftDKCoinsFormatter());

        registerCommandsAndListeners();
        registerUserManagerCacheQueries();
        registerPlayerAdapter();

        initAccountManager(dkCoins);
        initCurrencyManager(dkCoins);
        setupMigration(dkCoins);
        DKCoinsConfig.init();

        if(McNative.getInstance().isNetworkAvailable()) {
            McNative.getInstance().getNetwork().getMessenger().registerChannel("general", this, new DKCoinsGeneralMessagingChannel());
        }

    }

    private void loadConfig() {
        getConfiguration().load(DKCoinsConfig.class);
        getLogger().info("DKCoins config loaded");
    }

    private void registerPlayerAdapter() {
        McNative.getInstance().getPlayerManager().registerPlayerAdapter(DKCoinsUser.class, minecraftPlayer -> {
            DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(minecraftPlayer.getUniqueId());
            user.getDefaultAccount();
            return user;
        });
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
        VariableDescriberRegistry.registerDescriber(DefaultAccountTransaction.class);

        VariableDescriber<DefaultAccountMemberRole> roleVariableDescriber = VariableDescriberRegistry.registerDescriber(DefaultAccountMemberRole.class);
        roleVariableDescriber.registerFunction("parentRoleName", role -> role.getParentRole() == null ? "none" : role.getParentRole().getName());

        VariableDescriberRegistry.registerDescriber(DefaultAccountCredit.class);
        VariableDescriberRegistry.registerDescriber(DefaultAccountLimitation.class);
        VariableDescriberRegistry.registerDescriber(DefaultRankedAccountCredit.class);

        VariableDescriber<DefaultBankAccount> describer = VariableDescriberRegistry.registerDescriber(DefaultBankAccount.class);
        ColoredString.makeFunctionColored(describer,"displayName");

        VariableDescriber<DefaultAccountMember> describer2 = VariableDescriberRegistry.registerDescriber(DefaultAccountMember.class);
        ColoredString.makeFunctionColored(describer2,"displayName");

        VariableDescriberRegistry.registerDescriber(AccessRight.class);
    }


    private void setupMigration(DKCoins dkCoins) {
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

    private void registerUserManagerCacheQueries() {
        Cache<DKCoinsUser> userCache = DefaultDKCoins.getInstance().getUserManager().getUserCache();
        userCache.registerQuery("byUUIDAndName", new DKCoinsUserUUIDAndNameQuery())
                .registerQuery("byUUID", new DKCoinsUserUUIDQuery())
                .registerQuery("byName", new DKCoinsUserNameQuery());
    }

    public void broadcastNetworkAction(String action, Document data) {
        if(McNative.getInstance().isNetworkAvailable()) {
            McNative.getInstance().getNetwork().getMessenger().sendMessage(NetworkIdentifier.BROADCAST, "general",
                    Document.newDocument().add("action", action).add("data", data));
        }
    }

    public void broadcastNetworkAction(String action) {
        broadcastNetworkAction(action, Document.newDocument());
    }

    public static DKCoinsPlugin getInstance() {
        return INSTANCE;
    }


    public static class DKCoinsUserUUIDAndNameQuery implements CacheQuery<DKCoinsUser> {

        @Override
        public boolean check(DKCoinsUser user, Object[] identifiers) {
            return user.getUniqueId().equals(identifiers[0]) && user.getName().equalsIgnoreCase((String) identifiers[1]);
        }

        @Override
        public void validate(Object[] identifiers) {
            Validate.isTrue(identifiers.length == 2 && identifiers[0] instanceof UUID && identifiers[1] instanceof String);
        }

        @Override
        public DKCoinsUser load(Object[] identifiers) {
            return new DefaultDKCoinsUser((UUID) identifiers[0], (String) identifiers[1]);
        }
    }

    public static class DKCoinsUserUUIDQuery implements CacheQuery<DKCoinsUser> {

        @Override
        public boolean check(DKCoinsUser user, Object[] identifiers) {
            return user.getUniqueId().equals(identifiers[0]);
        }

        @Override
        public void validate(Object[] identifiers) {
            Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof UUID);
        }

        @Override
        public DKCoinsUser load(Object[] identifiers) {
            UUID playerId = (UUID) identifiers[0];
            String name = McNative.getInstance().getPlayerManager().getPlayer(playerId).getName();
            return new DefaultDKCoinsUser(playerId, name);
        }
    }

    public static class DKCoinsUserNameQuery implements CacheQuery<DKCoinsUser> {

        @Override
        public boolean check(DKCoinsUser user, Object[] identifiers) {
            return user.getName().equalsIgnoreCase((String) identifiers[0]);
        }

        @Override
        public void validate(Object[] identifiers) {
            Validate.isTrue(identifiers.length == 1 && identifiers[0] instanceof String);
        }

        @Override
        public DKCoinsUser load(Object[] identifiers) {
            String name = (String) identifiers[0];
            MinecraftPlayer player = McNative.getInstance().getPlayerManager().getPlayer(name);
            if(player == null) return null;
            return new DefaultDKCoinsUser(player.getUniqueId(), name);
        }
    }
}
