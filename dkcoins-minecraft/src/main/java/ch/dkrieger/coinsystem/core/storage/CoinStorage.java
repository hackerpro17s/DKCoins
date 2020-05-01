package ch.dkrieger.coinsystem.core.storage;

import ch.dkrieger.coinsystem.core.player.CoinPlayer;

import java.util.List;

public interface CoinStorage {

    boolean connect();

    void disconnect();

    boolean isConnected();

    List<CoinPlayer> getPlayers();
}