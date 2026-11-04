# ATM Simulator (Java Swing + MySQL + JDBC + Maven)

A desktop ATM application with a professional Swing GUI, MySQL persistence, salted PBKDF2 PIN hashing,
transactional money movement and layered OOP design (UI -> Service -> DAO -> Database).

## Features
| Screen | What it does |
|---|---|
| Login | Account number + PIN. Account locks after 3 wrong PINs. |
| Signup | Creates user + unique 12-digit account number + hashed PIN, optional opening deposit. |
| Dashboard | Sidebar navigation, quick-action tiles, auto-logout after 3 min of inactivity. |
| Balance Enquiry | Live balance read from the database. |
| Deposit | Validated amount, max 1,00,000 per transaction. |
| Withdrawal | Multiples of 100, max 50,000, PIN re-confirmation, insufficient-funds check. |
| Money Transfer | Recipient lookup + confirmation dialog, PIN re-confirmation, atomic debit/credit. |
| Mini Statement | Last 5 transactions. |
| PIN Change | Verifies current PIN, rejects weak PINs (1111, 1234 ...), stores a new salt + hash. |
| Transaction History | Filter by type and date range (latest 500 rows). |

## Requirements
* JDK 17 or newer
* MySQL 8.0.16+ (CHECK constraints are used)
* Maven 3.8+ (or the VS Code "Extension Pack for Java", which can run the project)

## Setup (5 steps)
1. **Create the database**
   ```bash
   mysql -u root -p < sql/atm_db.sql
   ```
   (or open `sql/atm_db.sql` in MySQL Workbench and run it). This creates `atm_db`, its tables
   and two demo accounts.
2. **Configure the connection** in `src/main/resources/db.properties`
   (`db.url`, `db.user`, `db.password`). You can also set the environment variables
   `ATM_DB_URL`, `ATM_DB_USER`, `ATM_DB_PASSWORD`, which take priority.
3. **Open the folder in VS Code** (`File > Open Folder > atm-simulator`). Let the Java extension import the Maven project.
4. **Run**: open `src/main/java/com/atm/Main.java` and click *Run*, or press F5 (the "Run ATM Simulator" configuration is included).
5. Or from a terminal:
   ```bash
   mvn clean compile exec:java          # run directly
   mvn clean package                    # build target/atm-simulator.jar (fat jar)
   java -jar target/atm-simulator.jar   # run the jar
   ```

## Demo accounts
| Account number | PIN | Holder |
|---|---|---|
| 100000000001 | 4826 | Aarav Sharma |
| 100000000002 | 7391 | Priya Patel |

Or click **Create an account** on the login screen to sign up.

## Project structure
```
atm-simulator/
|-- pom.xml
|-- README.md
|-- sql/atm_db.sql                      database + tables + demo data
|-- .vscode/{settings,launch}.json
`-- src/main/
    |-- resources/db.properties         connection settings
    `-- java/com/atm/
        |-- Main.java                   entry point
        |-- config/DatabaseConfig.java  JDBC connection + transaction helper
        |-- exception/                  ATMException, ValidationException, AuthenticationException,
        |                               AccountLockedException, InsufficientFundsException, DatabaseException
        |-- model/                      User, Account, Transaction, TransactionType
        |-- dao/                        UserDAO, AccountDAO, TransactionDAO (PreparedStatements only)
        |-- service/                    AuthService, AccountService (business rules, transactions)
        |-- util/                       PinHasher, Validator, Format, Session
        `-- ui/                         Theme, RoundedButton, InputFilter, Async, BasePanel, BrandPanel,
                                        LoginFrame, SignupFrame, DashboardFrame, HomePanel, BalancePanel,
                                        DepositPanel, WithdrawPanel, TransferPanel, MiniStatementPanel,
                                        HistoryPanel, PinChangePanel, TransactionTable
```

## Security & reliability notes
* PINs are hashed with PBKDF2-HMAC-SHA256 (120,000 iterations) and a random 16-byte salt per account; verification is constant-time. PIN arrays are wiped after use.
* All SQL uses `PreparedStatement` (no SQL injection).
* Deposits, withdrawals and transfers run in a database transaction with `SELECT ... FOR UPDATE` row locks; transfers lock rows in a fixed order to avoid deadlocks. Any failure rolls back.
* The `accounts.balance >= 0` CHECK constraint is a second line of defence behind the application checks.
* Database work runs on a `SwingWorker` thread, so the UI never freezes.
* Technical errors are caught and shown as friendly messages; stack traces never reach the user.

## Troubleshooting
* **"Database error. Please make sure MySQL is running..."** - MySQL is down or `db.properties` is wrong.
* **`Access denied for user`** - fix `db.user` / `db.password`.
* **`Unknown database 'atm_db'`** - run `sql/atm_db.sql` first.
* The rupee symbol needs a font that includes it (all modern Windows/macOS/Linux systems do).
