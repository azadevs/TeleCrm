package org.telegram.tgcrm.model;

import com.google.firebase.Timestamp;

/**
 * Created by : Azamat Kalmurzaev
 * 08/03/25
 */
public class Customer {
    public String name;
    public String phone;
    public String type;
    public Timestamp date;
    public String userId=null;
    public String description=null;

    public Customer(String name, String phone, String type) {
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.date = Timestamp.now();
    }

}
