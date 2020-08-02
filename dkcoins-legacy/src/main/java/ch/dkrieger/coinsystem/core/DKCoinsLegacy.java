/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 02.08.20, 20:44
 * @web %web%
 *
 * The DKCoins Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

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
        return INSTANCE;
    }

    public static void setInstance(DKCoinsLegacy instance) {
        if(INSTANCE != null) throw new IllegalArgumentException("DKCoins legacy instance is already set");
        INSTANCE = instance;
    }
}
