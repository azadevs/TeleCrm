package org.telegram.tgcrm.model;

/**
 * Created by : Azamat Kalmurzaev
 * 14/03/25
 */
public class BalanceOfWorker {

    public long fullPaid = 0L;
    public long moneyStillInProcess = 0L;
    public long readyToWithdrawn = 0L;
    public String username;

    public BalanceOfWorker(long fullPaid, long moneyStillInProcess, long readyToWithdrawn, String username) {
        this.fullPaid = fullPaid;
        this.moneyStillInProcess = moneyStillInProcess;
        this.readyToWithdrawn = readyToWithdrawn;
        this.username = username;
    }

    public BalanceOfWorker(){}


}
