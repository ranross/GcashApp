package gcashapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * CashIn.java  (Job Sheet 2-3)
 * -------------------------------------------------------
 * Adds money to a user's balance and records the deposit as a row
 * in the "transactions" table.
 *
 * transferToID / transferFromID are left as 0 for a cash-in, since
 * money isn't moving between two accounts - it's coming from outside
 * the system (e.g. loading via a partner store), same as real Gcash.
 */
public class CashIn {

    /*
     * Adds `amount` to userId's balance and logs the transaction.
     * Returns the new balance, or -1 if the cash-in failed.
     */
    public double cashIn(int userId, double amount, String name) {

        if (amount <= 0) {
            System.out.println("[Cash-In Failed] Amount must be greater than 0.");
            return -1;
        }

        String checkUserSql = "SELECT amount FROM balance WHERE user_id = ?";
        String updateBalanceSql = "UPDATE balance SET amount = amount + ? WHERE user_id = ?";
        String insertTxnSql =
            "INSERT INTO transactions (amount, name, account_id, date, transferToID, transferFromID) " +
            "VALUES (?, ?, ?, ?, 0, 0)";

        try (Connection conn = DatabaseConnection.connect()) {

            // Step 1: make sure this user actually has a balance row
            double currentBalance;
            try (PreparedStatement ps = conn.prepareStatement(checkUserSql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("[Cash-In Failed] User ID " + userId + " does not exist.");
                    return -1;
                }
                currentBalance = rs.getDouble("amount");
            }

            // Step 2: update the balance
            try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            // Step 3: record the transaction with today's date/time
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            try (PreparedStatement ps = conn.prepareStatement(insertTxnSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, name);
                ps.setInt(3, userId);
                ps.setString(4, timestamp);
                ps.executeUpdate();
            }

            double newBalance = currentBalance + amount;
            System.out.println("[Cash-In Success] PHP " + amount + " added. New balance: PHP " + newBalance);
            return newBalance;

        } catch (SQLException e) {
            System.out.println("[Cash-In Failed] Database error: " + e.getMessage());
            return -1;
        }
    }
}
