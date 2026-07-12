# GcashApp — Job Sheets 2-1 through 2-6 (Complete Banking App)

This is a complete, runnable command-line banking app built for Job
Sheets 2-1 to 2-6. It's written and heavily commented for people who
have **never coded in Java before**, but this version uses a **real
database** through the **Java SE 8 JDBC API**, as Job Sheets 2-2
through 2-6 require.

> **Note:** This replaces the earlier simple version of GcashApp
> (Job Sheet 2-1 only, in-memory data). This version keeps the same
> ideas but now actually saves everything to a database file, and
> combines all six job sheets into one working program.

## What's inside

```
GcashApp/
 ├─ src/gcashapp/
 │   ├─ DatabaseConnection.java   Sets up gcash.db and hands out JDBC connections
 │   ├─ User.java                 Model for one row of the "users" table
 │   ├─ UserAuthentication.java   Job Sheet 2-1: register, login, changePin, logout
 │   ├─ CheckBalance.java         Job Sheet 2-2: checkBalance(userID)
 │   ├─ Transaction.java          Model for one row of the "transactions" table
 │   ├─ CashIn.java               Job Sheet 2-3: cashIn(userID, amount, name)
 │   ├─ CashTransfer.java         Job Sheet 2-4: cashTransfer(from, to, amount)
 │   ├─ Transactions.java         Job Sheet 2-5: viewAll / viewUserAll / viewTransaction
 │   └─ Main.java                 Job Sheet 2-6: combines everything into a CLI app
 ├─ lib/
 │   └─ sqlite-jdbc-3.36.0.3.jar  The JDBC driver (see "What is this jar" below)
 └─ README.md
```

All nine `.java` files share **one package**, `gcashapp` (Job Sheet
2-6, step 2), which is why `Main.java` can use every other class
without needing an `import` statement — same-package classes always
see each other automatically.

## The database

We use **SQLite** because it needs zero setup — the whole database
is one file, `gcash.db`, that gets created automatically the first
time you run the program (right next to wherever you run it from).
Every table required by the job sheets is created automatically:

- `users` — id, name, email, number, pin
- `balance` — id, amount, user_id
- `transactions` — id, amount, name, account_id, date, transferToID, transferFromID
  *(named "transactions", plural, because "transaction" is a reserved SQL keyword)*

Two sample accounts are added automatically the first time you run
it, so you have something to log in with right away:

| Email | Number | PIN | Starting Balance |
|---|---|---|---|
| juan@email.com | 09171234567 | 1234 | PHP 500 |
| ana@email.com | 09201234567 | 4321 | PHP 100 |

## What is that `.jar` file in `lib/`?

Java doesn't know how to talk to SQLite out of the box — it needs a
small add-on called a **JDBC driver**. `sqlite-jdbc-3.36.0.3.jar` is
that add-on. You don't need to open or edit it; you just need to tell
Java it exists when you compile and run the program (shown below).

## How to run it

### Option A — Using an IDE like IntelliJ IDEA (easiest)
1. Install **IntelliJ IDEA Community Edition** (free) from jetbrains.com.
2. Open it → **New Project** → name it `GcashApp` → choose Java.
3. Copy the `src/gcashapp/` folder into your project's `src` folder, and copy `lib/sqlite-jdbc-3.36.0.3.jar` into a `lib` folder in your project.
4. Right-click `lib/sqlite-jdbc-3.36.0.3.jar` in the project panel → **Add as Library**.
5. Right-click `Main.java` → **Run**.
6. Type your answers into the console at the bottom of the screen.

### Option B — Using the command line
1. Install a JDK (version 17+) from [adoptium.net](https://adoptium.net).
2. Keep the folder structure shown above (`src/gcashapp/*.java` and `lib/sqlite-jdbc-3.36.0.3.jar`).
3. Open a terminal in the `GcashApp` folder and run:

   **Windows:**
   ```
   javac -cp lib\sqlite-jdbc-3.36.0.3.jar -d bin src\gcashapp\*.java
   java -cp "bin;lib\sqlite-jdbc-3.36.0.3.jar" gcashapp.Main
   ```

   **Mac/Linux:**
   ```
   javac -cp lib/sqlite-jdbc-3.36.0.3.jar -d bin src/gcashapp/*.java
   java -cp "bin:lib/sqlite-jdbc-3.36.0.3.jar" gcashapp.Main
   ```

4. Follow the on-screen prompts: log in (or type `register` to make
   a new account), then choose Check Balance / Cash-In / Transfer /
   View Transactions / Logout as many times as you like.

## How each job sheet maps to the code

- **2-1 (User Authentication)** → `UserAuthentication.java` — register, validate, login (by email or number), track failed PIN attempts with a 3-strike lockout, changePin, logout.
- **2-2 (Check Balance)** → `CheckBalance.java` — reads the `balance` table by `user_id`.
- **2-3 (Cash-In)** → `CashIn.java` — adds an amount to a balance and logs it as a transaction. Tested in the CLI by cashing in 200 then 300.
- **2-4 (Cash-Transfer)** → `CashTransfer.java` — moves money between two users' balances, with restrictions: positive amount, no self-transfer, recipient must exist, sender must have enough balance.
- **2-5 (View-Transaction)** → `Transactions.java` — `viewAll()`, `viewUserAll(userID)`, `viewTransaction(transactionID)`.
- **2-6 (Combined Program)** → `Main.java` — logs in, then loops through a menu (Check Balance / Cash-In / Transfer / View Transactions) until the user logs out.

A full spreadsheet breakdown of every single requirement (step by
step, per job sheet) is included separately as an Excel file.
