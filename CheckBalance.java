package gcashapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * CheckBalance.java  (Job Sheet 2-2)
 * -------------------------------------------------------
 * Reads the current balance for a given user from the "balance"
 * table (fields: id, amount, user_id) using JDBC.
 */
public class CheckBalance {

    /*
     * Returns the balance for the given userID, or -1 if that user
     * has no balance row (e.g. userID doesn't exist).
     */
    public double checkBalance(int userId) {
        String sql = "SELECT amount FROM balance WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double amount = rs.getDouble("amount");
                System.out.println("[Check Balance] User ID " + userId + " balance: PHP " + amount);
                return amount;
            } else {
                System.out.println("[Check Balance Failed] No balance record found for User ID " + userId);
                return -1;
            }

        } catch (SQLException e) {
            System.out.println("[Check Balance Failed] Database error: " + e.getMessage());
            return -1;
        }
    }
}
