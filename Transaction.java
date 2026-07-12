package gcashapp;

/*
 * Transaction.java
 * -------------------------------------------------------
 * Represents ONE ROW of the "transactions" table:
 * id | amount | name | account_id | date | transferToID | transferFromID
 *
 * transferToID / transferFromID are 0 when a transaction is a plain
 * cash-in (not a transfer between two accounts).
 */
public class Transaction {

    private int id;
    private double amount;
    private String name;
    private int accountId;
    private String date;
    private int transferToId;
    private int transferFromId;

    public Transaction(int id, double amount, String name, int accountId,
                        String date, int transferToId, int transferFromId) {
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.accountId = accountId;
        this.date = date;
        this.transferToId = transferToId;
        this.transferFromId = transferFromId;
    }

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getName() { return name; }
    public int getAccountId() { return accountId; }
    public String getDate() { return date; }
    public int getTransferToId() { return transferToId; }
    public int getTransferFromId() { return transferFromId; }

    @Override
    public String toString() {
        String base = "Txn #" + id + " | PHP " + amount + " | " + name + " | Account: " + accountId + " | " + date;
        if (transferFromId > 0 || transferToId > 0) {
            base += " | Transfer From: " + transferFromId + " -> To: " + transferToId;
        }
        return base;
    }
}
