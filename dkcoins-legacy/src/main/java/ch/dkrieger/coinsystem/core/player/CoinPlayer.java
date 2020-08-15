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

package ch.dkrieger.coinsystem.core.player;


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



	public long getCoins(){
		return this.coins;
	}
}
