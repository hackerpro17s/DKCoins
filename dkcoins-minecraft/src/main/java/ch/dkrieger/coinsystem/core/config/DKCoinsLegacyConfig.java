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
