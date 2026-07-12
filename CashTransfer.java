package gcashapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * CashTransfer.java  (Job Sheet 2-4)
 * -------------------------------------------------------
 * Moves money from the CURRENTLY LOGGED IN user's balance to
 * another user's balance, then records ONE transaction row showing
 * both sides of the transfer (transferFromID / transferToID).
 *
 * "Create multiple restrictions and use case prompts" (step 3 of the
 * job sheet) is handled by the checks below - each one prints a
 * clear, specific message so the user understands exactly why a
 * transfer did or didn't go through.
 */
public class CashTransfer {

    /*
     * Transfers `amount` from fromUserId to toUserId.
     * Returns true if the transfer succeeded, false otherwise.
     */
    public boolean cashTransfer(int fromUserId, int toUserId, double amount) {

        // ---- Restriction 1: amount must be positive ----
        if (amount <= 0) {
            System.out.println("[Transfer Failed] Amount must be greater than 0.");
            return false;
        }

        // ---- Restriction 2: can't transfer to your own account ----
        if (fromUserId == toUserId) {
            System.out.println("[Transfer Failed] You cannot transfer money to your own account.");
            return false;
        }

        String checkBalanceSql = "SELECT amount FROM balance WHERE user_id = ?";
        String updateSenderSql = "UPDATE balance SET amount = amount - ? WHERE user_id = ?";
        String updateReceiverSql = "UPDATE balance SET amount = amount + ? WHERE user_id = ?";
        String insertTxnSql =
            "INSERT INTO transactions (amount, name, account_id, date, transferToID, transferFromID) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect()) {

            // ---- Restriction 3: receiving account must exist ----
            double receiverBalance;
            try (PreparedStatement ps = conn.prepareStatement(checkBalanceSql)) {
                ps.setInt(1, toUserId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("[Transfer Failed] Recipient account (User ID " + toUserId + ") does not exist.");
                    return false;
                }
                receiverBalance = rs.getDouble("amount");
            }

            // ---- Restriction 4: sender must exist AND have enough balance ----
            double senderBalance;
            try (PreparedStatement ps = conn.prepareStatement(checkBalanceSql)) {
                ps.setInt(1, fromUserId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("[Transfer Failed] Your account (User ID " + fromUserId + ") does not exist.");
                    return false;
                }
                senderBalance = rs.getDouble("amount");
            }

            if (senderBalance < amount) {
                System.out.println("[Transfer Failed] Insufficient balance. You only have PHP " + senderBalance + ".");
                return false;
            }

            // ---- All checks passed - perform the transfer ----
            try (PreparedStatement ps = conn.prepareStatement(updateSenderSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, fromUserId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateReceiverSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, toUserId);
                ps.executeUpdate();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            try (PreparedStatement ps = conn.prepareStatement(insertTxnSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, "Cash Transfer");
                ps.setInt(3, fromUserId);
                ps.setString(4, timestamp);
                ps.setInt(5, toUserId);
                ps.setInt(6, fromUserId);
                ps.executeUpdate();
            }

            System.out.println("[Transfer Success] PHP " + amount + " sent from User ID " + fromUserId + " to User ID " + toUserId + ".");
            System.out.println("Your new balance: PHP " + (senderBalance - amount));
            return true;

        } catch (SQLException e) {
            System.out.println("[Transfer Failed] Database error: " + e.getMessage());
            return false;
        }
    }
}
