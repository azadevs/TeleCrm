package org.telegram.tgcrm.model;

/**
 * Created by : Azamat Kalmurzaev
 * 26/02/25
 */
public class UserCredentials {

    public String id;
    public String username;
    public String password;

    public UserCredentials(){};
    public UserCredentials(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
}
