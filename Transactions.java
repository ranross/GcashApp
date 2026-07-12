package gcashapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 * Transactions.java  (Job Sheet 2-5)
 * -------------------------------------------------------
 * Read-only "viewing" methods for the transactions table:
 *   viewAll()          -> every transaction in the whole bank
 *   viewUserAll(userID) -> every transaction that touched one user
 *   viewTransaction(id)  -> one specific transaction by its own ID
 */
public class Transactions {

    // Turns one row of a ResultSet into a Transaction object.
    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("id"),
            rs.getDouble("amount"),
            rs.getString("name"),
            rs.getInt("account_id"),
            rs.getString("date"),
            rs.getInt("transferToID"),
            rs.getInt("transferFromID")
        );
    }

    /*
     * Returns EVERY transaction in the bank, newest first.
     */
    public List<Transaction> viewAll() {
        List<Transaction> results = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("[View All Failed] Database error: " + e.getMessage());
        }
        return results;
    }

    /*
     * Returns every transaction that involves ONE user - whether they
     * were the one who cashed in, sent a transfer, or received one.
     */
    public List<Transaction> viewUserAll(int userId) {
        List<Transaction> results = new ArrayList<>();
        String sql =
            "SELECT * FROM transactions " +
            "WHERE account_id = ? OR transferFromID = ? OR transferToID = ? " +
            "ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                results.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("[View User Transactions Failed] Database error: " + e.getMessage());
        }
        return results;
    }

    /*
     * Returns ONE transaction by its transaction ID, or null if it
     * doesn't exist.
     */
    public Transaction viewTransaction(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            } else {
                System.out.println("[View Transaction] No transaction found with ID " + transactionId);
                return null;
            }

        } catch (SQLException e) {
            System.out.println("[View Transaction Failed] Database error: " + e.getMessage());
            return null;
        }
    }
}
