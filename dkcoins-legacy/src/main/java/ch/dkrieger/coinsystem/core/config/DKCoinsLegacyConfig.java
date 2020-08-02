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

/*
 * created by Dkrieger on 24.02.18 11:44
 */

package ch.dkrieger.coinsystem.core.config;

import ch.dkrieger.coinsystem.core.storage.storage.StorageType;
import net.pretronic.libraries.document.annotations.DocumentIgnored;
import net.pretronic.libraries.document.annotations.DocumentKey;

public class DKCoinsLegacyConfig {

    @DocumentKey("storage.folder")
    public String dataFolder;

    @DocumentKey("storage.type")
    private String storageType0;

    @DocumentIgnored
    public StorageType storageType = null;

    @DocumentKey("storage.host")
    public String host;

    @DocumentKey("storage.port")
    public String port;

    @DocumentKey("storage.user")
    public String user;

    @DocumentKey("storage.password")
    public String password;

    @DocumentKey("storage.database")
    public String database;

    @DocumentKey("storage.mongodb.mongodbauthentication")
    public boolean mongodbAuthentication;

    @DocumentKey("storage.mongodb.authenticationDatabase")
    public String mongodbAuthenticationDatabase;

    @DocumentKey("storage.mongodb.srv")
    public boolean mongodbSrv;

    public void init() {
        this.storageType = StorageType.parse(this.storageType0);
    }
}
