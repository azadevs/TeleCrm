package org.telegram.tgcrm.model;

import java.util.List;

public class UserActions {
    public String username;
    public List<ActionEntity> actions;

    public UserActions(String username, List<ActionEntity> actions) {
        this.username = username;
        this.actions = actions;
    }
}
