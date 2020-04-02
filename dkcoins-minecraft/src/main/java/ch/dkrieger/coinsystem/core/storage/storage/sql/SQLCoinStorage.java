package ch.dkrieger.coinsystem.core.storage.storage.sql;

import ch.dkrieger.coinsystem.core.DKCoinsLegacy;
import ch.dkrieger.coinsystem.core.config.DKCoinsLegacyConfig;
import ch.dkrieger.coinsystem.core.player.CoinPlayer;
import ch.dkrieger.coinsystem.core.storage.CoinStorage;
import ch.dkrieger.coinsystem.core.storage.storage.sql.table.Table;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 *
 *  * Copyright (c) 2018 Davide Wietlisbach on 24.11.18 15:55
 *
 */

public abstract class SQLCoinStorage implements CoinStorage {


    
    private DKCoinsLegacyConfig config;
    private HikariDataSource dataSource;
    private Table table;


    public SQLCoinStorage(DKCoinsLegacyConfig config) {
        this.config = config;
    }

    @Override
    public boolean isConnected() {
        try{
            return dataSource != null && !(dataSource.isClosed()) ;
        }catch (Exception ignored){}
        return false;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Table getTable() {
        return table;
    }

    public void setDataSource(HikariConfig config) {
        config.setPoolName(DKCoinsLegacy.PREFIX);

        config.addDataSourceProperty("cachePrepStmts",true);
        config.addDataSourceProperty("characterEncoding","utf-8");
        config.addDataSourceProperty("useUnicode",true);
        config.addDataSourceProperty("allowMultiQueries",true);

        config.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public boolean connect() {
        if(!isConnected()){
            loadDriver();
            System.out.println(DKCoinsLegacy.PREFIX + " connecting to SQL server at "+config.host+":"+config.port);
            try {
                connect(config);
                this.table = new Table(this, "DKCoins_players");// .create("PRIMARY KEY (`ID`)")
                createTable(table);
                System.out.println(DKCoinsLegacy.PREFIX + " successful connected to SQL server at "+config.host+":"+config.port);
            }catch (SQLException exception) {
                System.out.println(DKCoinsLegacy.PREFIX + " Could not connect to SQL server at "+config.host+":"+config.port);
                System.out.println(DKCoinsLegacy.PREFIX + "[DKCoinsLegacy] Error: "+exception.getMessage());
                System.out.println(DKCoinsLegacy.PREFIX + " Check your login data in the config.");
                return false;
            }
        }
        return true;
    }

    @Override
    public void disconnect() {
        if(isConnected()){
            dataSource.close();
            System.out.println(DKCoinsLegacy.PREFIX + " successful disconnected from sql server at "+this.config.host+":"+this.config.port);
        }
    }

    @Override
    public List<CoinPlayer> getPlayers(){
        return this.table.select().execute(result -> {
            List<CoinPlayer> players = new ArrayList<>();
            while (result.next()) {
                players.add(new CoinPlayer(result.getInt("ID"),
                        UUID.fromString(result.getString("uuid")),
                        result.getString("name"),
                        result.getString("color"),
                        result.getLong("firstLogin"),
                        result.getLong("lastLogin"),
                        result.getLong("coins")));
            }
            return players;
        });
    }

    public abstract void createTable(Table table);

    public abstract void connect(DKCoinsLegacyConfig config) throws SQLException;

    public abstract void loadDriver();
}
