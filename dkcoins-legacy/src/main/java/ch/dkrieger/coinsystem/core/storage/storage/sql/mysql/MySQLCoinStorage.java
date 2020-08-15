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

package ch.dkrieger.coinsystem.core.storage.storage.sql.mysql;

import ch.dkrieger.coinsystem.core.config.DKCoinsLegacyConfig;
import ch.dkrieger.coinsystem.core.storage.storage.sql.SQLCoinStorage;
import ch.dkrieger.coinsystem.core.storage.storage.sql.table.Table;
import com.zaxxer.hikari.HikariConfig;

import java.sql.SQLException;

/*
 *
 *  * Copyright (c) 2018 Philipp Elvin Friedhoff on 17.11.18 20:23
 *
 */

public class MySQLCoinStorage extends SQLCoinStorage {

	public MySQLCoinStorage(DKCoinsLegacyConfig config) {
        super(config);
	}

    @Override
    public void loadDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch(ClassNotFoundException exception) {
			System.out.println("[DKCoinsLegacy] Could not load MySQL driver.");
		}
	}
    @Override
    public void connect(DKCoinsLegacyConfig pluginConfig) throws SQLException {
		HikariConfig config = new HikariConfig();
		config.setUsername(pluginConfig.user);
		config.setPassword(pluginConfig.password);
		config.setJdbcUrl("jdbc:mysql://"+pluginConfig.host+":"+pluginConfig.port+"/"+pluginConfig.database);
		setDataSource(config);
    }

	@Override
	public void createTable(Table table) {
		table.create()
				.create("`ID` int NOT NULL AUTO_INCREMENT")
				.create("`uuid` varchar(120) NOT NULL")
				.create("`name` varchar(32) NOT NULL")
				.create("`color` varchar(32) NOT NULL")
				.create("`firstLogin` varchar(60) NOT NULL")
				.create("`lastLogin` varchar(60) NOT NULL")
				.create("`coins` int(200) NOT NULL")
				.create("PRIMARY KEY (`ID`)")
				.execute();
	}
}