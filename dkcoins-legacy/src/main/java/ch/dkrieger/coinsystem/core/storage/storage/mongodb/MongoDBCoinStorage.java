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

package ch.dkrieger.coinsystem.core.storage.storage.mongodb;

/*
 *
 *  * Copyright (c) 2018 Philipp Elvin Friedhoff on 16.11.18 19:49
 *
 */

import ch.dkrieger.coinsystem.core.config.DKCoinsLegacyConfig;
import ch.dkrieger.coinsystem.core.player.CoinPlayer;
import ch.dkrieger.coinsystem.core.storage.CoinStorage;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.List;

public class MongoDBCoinStorage implements CoinStorage {

    private DKCoinsLegacyConfig config;
    private MongoDatabase database;
    private MongoCollection collection;

    public MongoDBCoinStorage(DKCoinsLegacyConfig config) {
        this.config = config;
    }

    @Override
    public boolean connect() {
        String uri = "mongodb"+(config.mongodbSrv?"+srv":"")+"://";
        if(config.mongodbAuthentication) uri += config.user+":"+config.password+"@";
        uri += config.host+"/";
        if(config.mongodbAuthentication) uri += config.mongodbAuthenticationDatabase;
        uri += "?retryWrites=true&connectTimeoutMS=500&socketTimeoutMS=500";

        MongoClient mongoClient = new MongoClient(new MongoClientURI(uri));
        this.database = mongoClient.getDatabase(config.database);
        this.collection = database.getCollection("DKCoins_players");
        return true;
    }
    @Override
    public List<CoinPlayer> getPlayers() {
        return MongoDBUtil.findALL(collection,CoinPlayer.class);
    }

    @Override
    public void disconnect() {
        //No disconnect needed
    }
    @Override
    public boolean isConnected() {
        //Implement
        return true;
    }
}