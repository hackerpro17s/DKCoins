package ch.dkrieger.coinsystem.core.storage.storage.json;


import ch.dkrieger.coinsystem.core.config.DKCoinsLegacyConfig;
import ch.dkrieger.coinsystem.core.player.CoinPlayer;
import ch.dkrieger.coinsystem.core.storage.CoinStorage;
import com.google.gson.reflect.TypeToken;
import io.netty.util.internal.ConcurrentSet;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;

import java.io.File;
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
    private ConcurrentSet<CoinPlayer> players;

    public JsonCoinStorage(DKCoinsLegacyConfig config) {
        new File(config.dataFolder).mkdirs();
        this.file = new File(config.dataFolder, "players.json");
        if(file.exists() && file.isFile()) this.data = DocumentFileType.JSON.getReader().read(file);
        else {
            System.out.println("[DKCoinsLegacy] No player data");
            return;
        }
        this.players = this.data.getObject("players", new TypeToken<ConcurrentSet<CoinPlayer>>(){}.getType());
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