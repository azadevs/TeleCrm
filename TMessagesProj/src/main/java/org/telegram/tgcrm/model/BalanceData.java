package org.telegram.tgcrm.model;

import com.google.firebase.Timestamp;

/**
 * Created by : Azamat Kalmurzaev
 * 11/03/25
 */
public class BalanceData {

    public long dialogId;

    public int price;

    public String name;

    public Timestamp timestamp;

    public String owner;

    public BalanceData(long dialogId, int price, String name,String owner) {
        this.dialogId = dialogId;
        this.price = price;
        this.name=name;
        this.timestamp=Timestamp.now();
        this.owner=owner;
    }

    public BalanceData(){}
}
