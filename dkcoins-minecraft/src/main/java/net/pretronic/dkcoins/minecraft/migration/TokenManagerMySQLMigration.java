package net.pretronic.dkcoins.minecraft.migration;

import net.pretronic.databasequery.api.Database;
import net.pretronic.databasequery.api.collection.DatabaseCollection;
import net.pretronic.databasequery.api.driver.DatabaseDriver;
import net.pretronic.databasequery.api.driver.DatabaseDriverFactory;
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig;
import net.pretronic.databasequery.api.query.result.QueryResult;
import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.databasequery.sql.dialect.Dialect;
import net.pretronic.databasequery.sql.driver.config.SQLDatabaseDriverConfigBuilder;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.migration.Migration;
import net.pretronic.dkcoins.api.migration.MigrationResult;
import net.pretronic.dkcoins.api.migration.MigrationResultBuilder;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.entry.DocumentEntry;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.utility.Convert;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.player.profile.GameProfile;
import org.mcnative.runtime.api.player.profile.GameProfileLoader;
import org.mcnative.runtime.api.plugin.configuration.Configuration;
import org.mcnative.runtime.api.plugin.configuration.ConfigurationProvider;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenManagerMySQLMigration implements Migration {

    @Override
    public String getName() {
        return "TokenManager-MySQL";
    }

    @Override
    public MigrationResult migrate(Currency currency) {
        File configLocation = new File("plugins/TokenManager/config.yml");
        if(configLocation.exists()) {
            Document config = DocumentFileType.YAML.getReader().read(configLocation);
            Document databaseConfiguration = config.getDocument("data.mysql");
            System.out.println(DocumentFileType.JSON.getWriter().write(databaseConfiguration, true));
            if(databaseConfiguration == null) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] Database configuration for TokenManager not found.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }
            if(!databaseConfiguration.getBoolean("enabled")) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] Database not enabled.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }
            String url = databaseConfiguration.getString("url");
            if(url == null) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] Url is null.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }
            String hostname = databaseConfiguration.getString("hostname");
            if(hostname == null) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] hostname is null.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }
            String port = databaseConfiguration.getString("port");
            if(port == null) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] port is null.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }
            String username = databaseConfiguration.getString("username");
            if(username == null) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] username is null.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }
            String password = databaseConfiguration.getString("password");
            if(password == null) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] password is null.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }
            String database = databaseConfiguration.getString("database");
            if(database == null) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] database is null.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }
            String table = databaseConfiguration.getString("table");
            if(table == null) {
                DKCoins.getInstance().getLogger().error("[TokenManager-MySQL-Migration] table is null.");
                return new MigrationResultBuilder().setSuccess(false).build();
            }

            PlayerDataProvider playerDataProvider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
            GameProfileLoader gameProfileLoader = McNative.getInstance().getRegistry().getService(GameProfileLoader.class);

            long start = System.currentTimeMillis();
            AtomicInteger totalCount = new AtomicInteger();
            AtomicInteger mcNativeCount = new AtomicInteger();
            AtomicInteger dkcoinsCount = new AtomicInteger();
            AtomicInteger skipped = new AtomicInteger();

            DatabaseDriverConfig<?> driverConfig = new SQLDatabaseDriverConfigBuilder()
                    .setConnectionString(url.replace("%hostname%", hostname).replace("%port%", port).replace("%database%", database))
                    .setUsername(username)
                    .setPassword(password)
                    .setAddress(InetSocketAddress.createUnresolved(hostname, Integer.parseInt(port)))
                    .setDialect(Dialect.MYSQL)
                    .build();

            DatabaseDriver driver = DatabaseDriverFactory.create("TokenManager-MySQL-Migration", driverConfig);
            driver.connect();

            Database tokenManagerDatabase = driver.getDatabase(database);

            DatabaseCollection tokenManagerTable = tokenManagerDatabase.getCollection(table);

            int page = 1;
            QueryResult queryResult = null;
            while (!(queryResult = tokenManagerTable.find().page(page, 1000).execute()).isEmpty()) {
                page++;

                for (QueryResultEntry resultEntry : queryResult) {


                    UUID playerId = Convert.toUUID(resultEntry.getString("uuid"));
                    String name = null;

                    int balance = resultEntry.getInt("tokens");

                    if(McNative.getInstance().getPlayerManager().getPlayer(playerId) == null) {
                        System.out.println(playerId);
                        GameProfile profile = gameProfileLoader.getGameProfile(playerId);
                        if(profile == null) {
                            skipped.incrementAndGet();
                            continue;
                        }
                        name = profile.getName();
                        playerDataProvider.createPlayerData(profile.getName(), playerId, -1, -1, -1, null);
                        mcNativeCount.incrementAndGet();
                    }

                    DKCoinsUser user = DKCoins.getInstance().getUserManager().getUser(playerId);

                    if(user != null) {
                        if(DKCoins.getInstance().getAccountManager().getAccount(name, "User") == null) {
                            BankAccount account = DKCoins.getInstance().getAccountManager().createAccount(name,
                                    DKCoins.getInstance().getAccountManager().searchAccountType("User"),
                                    false, null, DKCoins.getInstance().getUserManager().getUser(playerId));
                            account.getCredit(currency).setAmount(balance);
                            dkcoinsCount.incrementAndGet();
                        }

                        totalCount.getAndIncrement();
                    } else {
                        DKCoins.getInstance().getLogger().warn("Skipped migration for user with uuid [{}]", playerId);
                        skipped.incrementAndGet();
                    }

                }
            }
            driver.disconnect();




            return new MigrationResultBuilder()
                    .setSuccess(true)
                    .setTotalMigrateCount(totalCount.get())
                    .setDkcoinsAccountMigrateCount(dkcoinsCount.get())
                    .setMcNativeMigrateCount(mcNativeCount.get())
                    .setSkipped(skipped.get())
                    .setTime(System.currentTimeMillis() - start)
                    .build();
        }
        return new MigrationResultBuilder()
                .setSuccess(false)
                .build();
    }
}
