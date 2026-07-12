package gcashapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/*
 * UserAuthentication.java  (Job Sheet 2-1)
 * -------------------------------------------------------
 * Same responsibilities as before (register, validate, login,
 * changePin, logout) but now backed by the REAL "users" table in
 * gcash.db through JDBC, instead of an in-memory list.
 *
 * Every method follows the same JDBC pattern:
 *   1. Open a Connection (try-with-resources closes it automatically)
 *   2. Prepare a PreparedStatement with "?" placeholders
 *   3. Fill in the placeholders with setString()/setInt() etc.
 *   4. Run it with executeQuery() (for SELECT) or executeUpdate()
 *      (for INSERT/UPDATE/DELETE)
 */
public class UserAuthentication {

    // Tracks failed PIN attempts per user (in memory - resets each run)
    private Map<Integer, Integer> failedAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    // Who is currently logged in? -1 = nobody.
    private int currentLoggedInUserId = -1;


    // ================= VALIDATION =================
    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }

    private boolean isValidNumber(String number) {
        return number != null && number.matches("^09\\d{9}$");
    }

    private boolean isValidPin(String pin) {
        return pin != null && pin.matches("^\\d{4}$");
    }


    // ================= REGISTRATION =================
    /*
     * Inserts a new row into the users table (and gives them a
     * starting balance row of 0 in the balance table).
     * Returns the new user's ID, or -1 if something was invalid.
     */
    public int register(String name, String email, String number, String pin) {

        if (!isValidName(name)) {
            System.out.println("[Registration Failed] Name cannot be empty.");
            return -1;
        }
        if (!isValidEmail(email)) {
            System.out.println("[Registration Failed] Email format is invalid.");
            return -1;
        }
        if (!isValidNumber(number)) {
            System.out.println("[Registration Failed] Mobile number must be 11 digits starting with 09.");
            return -1;
        }
        if (!isValidPin(pin)) {
            System.out.println("[Registration Failed] PIN must be exactly 4 digits.");
            return -1;
        }

        String insertUserSql = "INSERT INTO users (name, email, number, pin) VALUES (?, ?, ?, ?)";
        String insertBalanceSql = "INSERT INTO balance (user_id, amount) VALUES (?, 0)";

        try (Connection conn = DatabaseConnection.connect()) {

            // Step 1: check for duplicate email/number first
            if (findUserByIdentifier(conn, email) != null || findUserByIdentifier(conn, number) != null) {
                System.out.println("[Registration Failed] Email or mobile number is already registered.");
                return -1;
            }

            // Step 2: insert the new user.
            // Statement.RETURN_GENERATED_KEYS lets us grab the new
            // auto-increment ID straight away.
            try (PreparedStatement ps = conn.prepareStatement(insertUserSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, number);
                ps.setString(4, pin);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                int newId = -1;
                if (keys.next()) {
                    newId = keys.getInt(1);
                }

                // Step 3: give the new user a starting balance row of 0
                try (PreparedStatement psBal = conn.prepareStatement(insertBalanceSql)) {
                    psBal.setInt(1, newId);
                    psBal.executeUpdate();
                }

                System.out.println("[Registration Success] Welcome, " + name + "! Your user ID is " + newId);
                return newId;
            }

        } catch (SQLException e) {
            System.out.println("[Registration Failed] Database error: " + e.getMessage());
            return -1;
        }
    }


    // ================= LOGIN =================
    /*
     * "identifier" can be an email OR a mobile number.
     * Returns: user ID on success, -1 no account found,
     *          -2 wrong PIN, -3 account locked.
     */
    public int login(String identifier, String pin) {

        try (Connection conn = DatabaseConnection.connect()) {

            User foundUser = findUserByIdentifier(conn, identifier);
            if (foundUser == null) {
                System.out.println("[Login Failed] No account found for that email/number.");
                return -1;
            }

            int attempts = failedAttempts.getOrDefault(foundUser.getId(), 0);
            if (attempts >= MAX_ATTEMPTS) {
                System.out.println("[Login Failed] Account locked due to too many wrong PIN attempts.");
                return -3;
            }

            if (!foundUser.getPin().equals(pin)) {
                attempts++;
                failedAttempts.put(foundUser.getId(), attempts);
                int remaining = MAX_ATTEMPTS - attempts;
                if (remaining <= 0) {
                    System.out.println("[Login Failed] Wrong PIN. Account is now locked.");
                    return -3;
                }
                System.out.println("[Login Failed] Wrong PIN. " + remaining + " attempt(s) remaining.");
                return -2;
            }

            failedAttempts.put(foundUser.getId(), 0);
            currentLoggedInUserId = foundUser.getId();
            System.out.println("[Login Success] Welcome back, " + foundUser.getName() + "! (User ID: " + foundUser.getId() + ")");
            return foundUser.getId();

        } catch (SQLException e) {
            System.out.println("[Login Failed] Database error: " + e.getMessage());
            return -1;
        }
    }


    // ================= CHANGE PIN =================
    public boolean changePin(String oldPin, String newPin) {

        if (currentLoggedInUserId == -1) {
            System.out.println("[Change PIN Failed] You must be logged in to change your PIN.");
            return false;
        }

        if (!isValidPin(newPin)) {
            System.out.println("[Change PIN Failed] New PIN must be exactly 4 digits.");
            return false;
        }

        String selectSql = "SELECT pin FROM users WHERE id = ?";
        String updateSql = "UPDATE users SET pin = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect()) {

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, currentLoggedInUserId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println("[Change PIN Failed] User not found.");
                    return false;
                }
                String currentPin = rs.getString("pin");

                if (!currentPin.equals(oldPin)) {
                    System.out.println("[Change PIN Failed] Old PIN is incorrect.");
                    return false;
                }
                if (currentPin.equals(newPin)) {
                    System.out.println("[Change PIN Failed] New PIN must be different from the old PIN.");
                    return false;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newPin);
                ps.setInt(2, currentLoggedInUserId);
                ps.executeUpdate();
            }

            System.out.println("[Change PIN Success] Your PIN has been updated.");
            return true;

        } catch (SQLException e) {
            System.out.println("[Change PIN Failed] Database error: " + e.getMessage());
            return false;
        }
    }


    // ================= LOGOUT =================
    public void logout() {
        if (currentLoggedInUserId == -1) {
            System.out.println("[Logout] No one is currently logged in.");
            return;
        }
        System.out.println("[Logout Success] Goodbye! (User ID " + currentLoggedInUserId + ")");
        currentLoggedInUserId = -1;
    }


    // ================= HELPERS =================
    public int getCurrentLoggedInUserId() {
        return currentLoggedInUserId;
    }

    // Looks up a user row by email OR mobile number. Used by both
    // register() (duplicate-check) and login().
    private User findUserByIdentifier(Connection conn, String identifier) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? OR number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("number"),
                    rs.getString("pin")
                );
            }
        }
        return null;
    }
}
