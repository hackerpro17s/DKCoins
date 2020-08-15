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

package ch.dkrieger.coinsystem.core.storage.storage.sql.sqlite;

/*
 *
 *  * Copyright (c) 2018 Philipp Elvin Friedhoff on 17.11.18 20:38
 *
 */

import ch.dkrieger.coinsystem.core.config.DKCoinsLegacyConfig;
import ch.dkrieger.coinsystem.core.storage.storage.sql.SQLCoinStorage;
import ch.dkrieger.coinsystem.core.storage.storage.sql.table.Table;
import com.zaxxer.hikari.HikariConfig;

import java.io.File;
import java.io.IOException;

public class SQLiteCoinStorage extends SQLCoinStorage {

    public SQLiteCoinStorage(DKCoinsLegacyConfig config) {
        super(config);
    }
    @Override
    public void connect(DKCoinsLegacyConfig config) {
        new File(config.dataFolder).mkdirs();
        try {
            new File(config.dataFolder,"players.db").createNewFile();
        } catch (IOException exception) {
            System.err.println("Could not create SQLite database file ("+exception.getMessage()+").");
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:"+config.dataFolder+"players.db");
        setDataSource(hikariConfig);
    }
    @Override
    public void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("[DKCoinsLegacy] Could not load SQLiteCoinStorage driver.");
        }
    }
    @Override
    public void createTable(Table table) {
        table.create()
                .create("`ID` INTEGER PRIMARY KEY AUTOINCREMENT")
                .create("`uuid` varchar(120) NOT NULL")
                .create("`name` varchar(32) NOT NULL")
                .create("`color` varchar(32) NOT NULL")
                .create("`firstLogin` varchar(60) NOT NULL")
                .create("`lastLogin` varchar(60) NOT NULL")
                .create("`coins` int(200) NOT NULL")
                .execute();
    }
}