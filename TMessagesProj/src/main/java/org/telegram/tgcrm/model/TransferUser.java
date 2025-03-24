package org.telegram.tgcrm.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

/**
 * Created by : Azamat Kalmurzaev
 * 07/03/25
 */
public class TransferUser {
    @PropertyName("other_user")
    public String otherUser;

    @PropertyName("current_user")
    public String currentUser;

    @PropertyName("transfer_time")
    public Timestamp transferTime;

    @PropertyName("dialog_id")
    public long dialogId;


    public TransferUser() {}

    public TransferUser(String otherUser, String currentUser, long dialogId){
        this.otherUser = otherUser;
        this.currentUser = currentUser;
        this.transferTime = Timestamp.now();;
        this.dialogId = dialogId;
    }
}

