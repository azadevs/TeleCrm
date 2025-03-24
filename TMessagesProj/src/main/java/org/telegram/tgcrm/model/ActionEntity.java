package org.telegram.tgcrm.model;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

@Keep
public class ActionEntity {

    @PropertyName("from_folder")
    public String fromFolder;

    @PropertyName("to_folder")
    public String toFolder;

    @PropertyName("action_owner")
    public String actionOwner;

    @PropertyName("dialog_id")
    public long dialogId;

    @PropertyName("dialog_name")
    public String dialogName;

    @PropertyName("action_time")
    public Timestamp actionTime;

    @PropertyName("folder_position")
    public int folderPosition;

    public ActionEntity(String fromFolder, String toFolder, String actionOwner, long dialogId,String dialogName,int folderPosition) {
        this.fromFolder = fromFolder;
        this.toFolder = toFolder;
        this.actionOwner = actionOwner;
        this.dialogId = dialogId;
        this.actionTime = Timestamp.now();
        this.dialogName = dialogName;
        this.folderPosition=folderPosition;
    }

    public ActionEntity(){}

}