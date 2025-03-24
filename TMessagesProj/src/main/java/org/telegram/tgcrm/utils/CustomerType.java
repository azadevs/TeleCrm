package org.telegram.tgcrm.utils;

/**
 * Created by : Azamat Kalmurzaev
 * 10/03/25
 */
public enum CustomerType {

    CUSTOMER_SEND("Tabriki yo'llangan mijoz"),

    CUSTOMER_DONT_SEND("Tabrik yo'llovchi mijoz");

    private final String type;

    CustomerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
