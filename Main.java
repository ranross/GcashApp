package gcashapp;

import java.util.List;
import java.util.Scanner;

/*
 * Main.java  (Job Sheet 2-6)
 * -------------------------------------------------------
 * This is the "start button" that combines every object we built in
 * Job Sheets 2-1 through 2-5 into one working command-line banking
 * app. Since every class lives in the SAME package (gcashapp), Java
 * already lets Main see all of them automatically - no import
 * statements are needed for our own classes. (If UserAuthentication,
 * CheckBalance, etc. lived in a DIFFERENT package, you would write
 * something like "import gcashapp.UserAuthentication;" at the top -
 * that's what step 1 of the job sheet is referring to.)
 *
 * PROGRAM FLOW (as required by the job sheet):
 *   1. Login
 *   2. Repeat: let the user Check Balance, Cash-In, Transfer, or
 *      View Transactions, as many times as they like
 *   3. Logout
 */
public class Main {

    // One Scanner is reused for all keyboard input in the program.
    private static Scanner scanner = new Scanner(System.in);

    // Declare ONE object of each class so every method below can use them.
    private static UserAuthentication auth = new UserAuthentication();
    private static CheckBalance checkBalanceService = new CheckBalance();
    private static CashIn cashInService = new CashIn();
    private static CashTransfer cashTransferService = new CashTransfer();
    private static Transactions transactionsService = new Transactions();

    public static void main(String[] args) {

        // Step 0: make sure the database and its tables exist.
        DatabaseConnection.initializeDatabase();

        System.out.println("=================================");
        System.out.println("   Welcome to GcashApp (Demo)");
        System.out.println("=================================");
        System.out.println("Sample accounts you can log in with:");
        System.out.println("  Email: juan@email.com | Number: 09171234567 | PIN: 1234");
        System.out.println("  Email: ana@email.com  | Number: 09201234567 | PIN: 4321");
        System.out.println("(Or type 'register' at the login prompt to create a new account.)\n");

        // ===== STEP 1: LOGIN =====
        int userId = -1;
        while (userId < 0) {
            System.out.print("Email or Mobile Number (or 'register'): ");
            String identifier = scanner.nextLine().trim();

            if (identifier.equalsIgnoreCase("register")) {
                doRegister();
                continue;
            }

            System.out.print("PIN: ");
            String pin = scanner.nextLine().trim();

            userId = auth.login(identifier, pin);

            if (userId < 0) {
                System.out.println("Please try again.\n");
            }
        }

        // ===== STEP 2: MAIN MENU LOOP =====
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n---------- MAIN MENU ----------");
            System.out.println("1. Check Balance");
            System.out.println("2. Cash-In");
            System.out.println("3. Transfer Money");
            System.out.println("4. View My Transactions");
            System.out.println("5. Logout");
            System.out.print("Choose an option (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    checkBalanceService.checkBalance(userId);
                    break;

                case "2":
                    doCashIn(userId);
                    break;

                case "3":
                    doTransfer(userId);
                    break;

                case "4":
                    doViewTransactions(userId);
                    break;

                case "5":
                    auth.logout();
                    loggedIn = false;
                    break;

                default:
                    System.out.println("Invalid option. Please choose 1-5.");
            }
        }

        System.out.println("\nThank you for using GcashApp!");
        scanner.close();
    }


    // ================= HELPER: REGISTER =================
    private static void doRegister() {
        System.out.println("\n--- New Account Registration ---");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Mobile Number (e.g. 09171234567): ");
        String number = scanner.nextLine().trim();
        System.out.print("4-digit PIN: ");
        String pin = scanner.nextLine().trim();

        auth.register(name, email, number, pin);
        System.out.println();
    }


    // ================= HELPER: CASH-IN =================
    private static void doCashIn(int userId) {
        System.out.print("Amount to cash-in: PHP ");
        double amount = readAmount();
        if (amount < 0) return; // readAmount() already printed the error

        cashInService.cashIn(userId, amount, "Cash In");
    }


    // ================= HELPER: TRANSFER =================
    /*
     * "Cash-in or transfer (based on available balance)" - before we
     * even ask for transfer details, we check the sender's current
     * balance. If it's 0, there is nothing to transfer, so we stop
     * early with a clear message instead of wasting the user's time.
     */
    private static void doTransfer(int userId) {
        double currentBalance = checkBalanceService.checkBalance(userId);

        if (currentBalance <= 0) {
            System.out.println("[Transfer] You have no available balance to transfer. Try Cash-In first.");
            return;
        }

        System.out.print("Recipient's User ID: ");
        int toUserId;
        try {
            toUserId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[Transfer Failed] User ID must be a number.");
            return;
        }

        System.out.print("Amount to transfer: PHP ");
        double amount = readAmount();
        if (amount < 0) return;

        cashTransferService.cashTransfer(userId, toUserId, amount);
    }


    // ================= HELPER: VIEW TRANSACTIONS =================
    private static void doViewTransactions(int userId) {
        List<Transaction> myTxns = transactionsService.viewUserAll(userId);

        if (myTxns.isEmpty()) {
            System.out.println("[Transactions] You have no transactions yet.");
            return;
        }

        System.out.println("--- Your Transactions ---");
        for (Transaction t : myTxns) {
            System.out.println(t);
        }
    }


    // ================= HELPER: SAFE NUMBER INPUT =================
    // Reads a line of input and turns it into a positive double.
    // Returns -1 (and prints a message) if the input wasn't valid.
    private static double readAmount() {
        String input = scanner.nextLine().trim();
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                System.out.println("Amount must be greater than 0.");
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("That doesn't look like a valid amount.");
            return -1;
        }
    }
}
