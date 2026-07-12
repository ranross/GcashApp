package gcashapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/*
 * DatabaseConnection.java
 * -------------------------------------------------------
 * This class is the ONE place in the whole program that knows how
 * to talk to the database. Every other class (UserAuthentication,
 * CheckBalance, CashIn, CashTransfer, Transactions) asks THIS class
 * for a Connection whenever it needs to read or write data.
 *
 * We use SQLite because it needs ZERO installation - the entire
 * database is just one file ("gcash.db") that gets created
 * automatically the first time you run the program, right next to
 * your .java files. This lets us use the real Java SE 8 JDBC API
 * (Connection, Statement, PreparedStatement, ResultSet) exactly like
 * you would with MySQL or any other database - the JDBC code you
 * write here would look almost identical for a "real" bank database.
 *
 * KEY JDBC VOCABULARY (for readers new to Java/JDBC):
 *   Connection        -> represents the open "phone line" to the database
 *   Statement         -> used to run a plain SQL command
 *   PreparedStatement -> a Statement with "?" placeholders you fill in
 *                        safely (this is what protects us from SQL
 *                        injection attacks - ALWAYS prefer this over
 *                        building SQL strings by hand)
 *   ResultSet         -> the table of rows that comes back from a SELECT
 */
public class DatabaseConnection {

    // This is the JDBC URL. "jdbc:sqlite:" tells Java which driver to
    // use, and "gcash.db" is just the filename of our database file.
    private static final String DB_URL = "jdbc:sqlite:gcash.db";

    /*
     * Opens (or creates) the connection to gcash.db.
     * Every method in this project calls this whenever it needs to
     * run a query, then closes it when it's done (using
     * "try-with-resources", which you'll see in the other classes -
     * it automatically closes the connection for you, even if an
     * error happens).
     */
    public static Connection connect() throws SQLException {
        // Explicitly load the SQLite JDBC driver class. Modern JDBC
        // usually finds drivers automatically, but calling this here
        // is the classic Java SE 8 style taught in most tutorials,
        // and it also protects us in case the automatic lookup
        // doesn't run in your particular setup.
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on the classpath.", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    /*
     * Creates all the required tables IF they don't already exist,
     * and adds some temporary dummy data the first time it runs
     * (this satisfies Job Sheet 2-2, step 3: "Add temporary dummy
     * data to your database").
     *
     * Call this ONCE at the very start of the program (Main.java
     * does this for you automatically).
     */
    public static void initializeDatabase() {
        String createUsers =
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id     INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name   TEXT NOT NULL," +
            "  email  TEXT NOT NULL UNIQUE," +
            "  number TEXT NOT NULL UNIQUE," +
            "  pin    TEXT NOT NULL" +
            ")";

        // NOTE: "transaction" is a reserved SQL word, so the actual
        // table is called "transactions" (plural) to stay safe.
        String createBalance =
            "CREATE TABLE IF NOT EXISTS balance (" +
            "  id      INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  amount  REAL NOT NULL DEFAULT 0," +
            "  user_id INTEGER NOT NULL UNIQUE," +
            "  FOREIGN KEY(user_id) REFERENCES users(id)" +
            ")";

        String createTransactions =
            "CREATE TABLE IF NOT EXISTS transactions (" +
            "  id             INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  amount         REAL NOT NULL," +
            "  name           TEXT," +
            "  account_id     INTEGER," +
            "  date           TEXT," +
            "  transferToID   INTEGER," +
            "  transferFromID INTEGER" +
            ")";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsers);
            stmt.execute(createBalance);
            stmt.execute(createTransactions);

            // Only add dummy data the FIRST time (so re-running the
            // program doesn't keep duplicating sample accounts).
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM users");
            if (rs.next() && rs.getInt("total") == 0) {
                insertDummyData(stmt);
                System.out.println("[Database] First run detected - dummy data added.");
            }

        } catch (SQLException e) {
            System.out.println("[Database Error] Could not set up the database: " + e.getMessage());
        }
    }

    // Adds a couple of sample users + starting balances, so you have
    // something to log in with right away (see README for the list).
    private static void insertDummyData(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT INTO users (name, email, number, pin) VALUES " +
            "('Juan Dela Cruz', 'juan@email.com', '09171234567', '1234')"
        );
        stmt.execute(
            "INSERT INTO users (name, email, number, pin) VALUES " +
            "('Ana Santos', 'ana@email.com', '09201234567', '4321')"
        );
        stmt.execute("INSERT INTO balance (user_id, amount) VALUES (1, 500.0)");
        stmt.execute("INSERT INTO balance (user_id, amount) VALUES (2, 100.0)");
    }
}
