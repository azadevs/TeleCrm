package org.telegram.tgcrm.model;

import java.util.List;

public class BalanceWithOwner {
    public String username;
    public List<BalanceData> balances;

    public BalanceWithOwner(String username, List<BalanceData> actions) {
        this.username = username;
        this.balances = actions;
    }
}
