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

package ch.dkrieger.coinsystem.core.storage.storage.json;


import ch.dkrieger.coinsystem.core.config.DKCoinsLegacyConfig;
import ch.dkrieger.coinsystem.core.player.CoinPlayer;
import ch.dkrieger.coinsystem.core.storage.CoinStorage;
import com.google.gson.reflect.TypeToken;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/*
 *
 *  * Copyright (c) 2018 Davide Wietlisbach on 24.11.18 16:20
 *
 */

public class JsonCoinStorage implements CoinStorage {

    private AtomicInteger nextID;
    private File file;
    private Document data;
    private Collection<CoinPlayer> players;

    public JsonCoinStorage(DKCoinsLegacyConfig config) {
        new File(config.dataFolder).mkdirs();
        this.file = new File(config.dataFolder, "players.json");
        if(file.exists() && file.isFile()) this.data = DocumentFileType.JSON.getReader().read(file);
        else {
            System.out.println("[DKCoinsLegacy] No player data");
            return;
        }
        this.players = this.data.getObject("players", new TypeToken<ArrayList<CoinPlayer>>(){}.getType());
        nextID = new AtomicInteger(this.players.size()+1);
    }

    @Override
    public boolean connect() {
        return true;
    }

    @Override
    public void disconnect() {}

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public List<CoinPlayer> getPlayers() {
        return new LinkedList<>(this.players);
    }
}