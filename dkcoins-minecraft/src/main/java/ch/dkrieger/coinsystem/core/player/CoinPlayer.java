package ch.dkrieger.coinsystem.core.player;

import net.md_5.bungee.api.ChatColor;

import java.util.UUID;

public class CoinPlayer {
	
	private int id;
	private String name, color;
	private UUID uuid;
	private long coins;
	private long firstLogin;
	private long lastLogin;
	
	public CoinPlayer(int id, UUID uuid, String name,String color, long firstLogin, long lastLogin, long coins){
		this.id = id;
		this.uuid = uuid;
		this.name = name;
		this.color = color;
		this.firstLogin = firstLogin;
		this.lastLogin = lastLogin;
		this.coins = coins;
	}
	public int getID(){
		return this.id;
	}

	public String getName(){
		return this.name;
	}

	public UUID getUUID(){
		return this.uuid;
	}


	public long getFirstLogin(){
		return this.firstLogin;
	}

	public long getLastLogin(){
		return this.lastLogin;
	}

	public String getColor(){
		return ChatColor.translateAlternateColorCodes('&',this.color);
	}

	public long getCoins(){
		return this.coins;
	}
}
