package ch.dkrieger.coinsystem.core;

import ch.dkrieger.coinsystem.core.config.DKCoinsLegacyConfig;
import ch.dkrieger.coinsystem.core.storage.CoinStorage;
import ch.dkrieger.coinsystem.core.storage.storage.StorageType;
import ch.dkrieger.coinsystem.core.storage.storage.json.JsonCoinStorage;
import ch.dkrieger.coinsystem.core.storage.storage.mongodb.MongoDBCoinStorage;
import ch.dkrieger.coinsystem.core.storage.storage.sql.mysql.MySQLCoinStorage;
import ch.dkrieger.coinsystem.core.storage.storage.sql.sqlite.SQLiteCoinStorage;
import net.pretronic.libraries.document.type.DocumentFileType;

import java.io.File;

public class DKCoinsLegacy {

    public static final String PREFIX = "[DKCoins] (Legacy-Migration)";
    private static DKCoinsLegacy INSTANCE;

    private CoinStorage storage;
    private final DKCoinsLegacyConfig config;

    public DKCoinsLegacy() {
        this.config = DocumentFileType.YAML.getReader().read(new File("plugins/DKCoins/legacy-config.yml")).getAsObject(DKCoinsLegacyConfig.class);
        this.config.init();

        setupStorage();
    }

    public CoinStorage getStorage() {
        return storage;
    }

    private void setupStorage() {
        if(this.config.storageType == StorageType.MYSQL) this.storage = new MySQLCoinStorage(this.config);
        else if(this.config.storageType == StorageType.SQLITE) this.storage = new SQLiteCoinStorage(this.config);
        else if(this.config.storageType == StorageType.MONGODB) this.storage = new MongoDBCoinStorage(this.config);
        else if(this.config.storageType == StorageType.JSON) this.storage = new JsonCoinStorage(this.config);

        if(this.storage != null && this.storage.connect()) {
            System.out.println(PREFIX + " Used Storage: "+this.config.storageType.toString());
            return;
        }
        System.out.println(PREFIX + " Used Backup Storage: "+StorageType.SQLITE.toString());
        this.storage = new SQLiteCoinStorage(this.config);
    }


    public static DKCoinsLegacy getInstance() {
        if(INSTANCE == null) throw new IllegalArgumentException("DKCoins legacy instance is null");
        return INSTANCE;
    }

    public static void setInstance(DKCoinsLegacy instance) {
        if(INSTANCE != null) throw new IllegalArgumentException("DKCoins legacy instance is already set");
        INSTANCE = instance;
    }
}
