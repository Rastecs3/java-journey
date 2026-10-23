import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/* ============================================================
 * 1) ABSTRACT BASE CLASS  ->  Account
 *    Holds what ALL accounts share, and defines the "rules"
 *    that subclasses must fill in (polymorphism).
 * ============================================================ */
abstract class Account {

    // ---- encapsulation: private, final where possible ----
    private final String accountNumber;
    private final String ownerName;
    private double balance;          // no setter => only changed through deposit/withdraw

    protected Account(String accountNumber, String ownerName, double openingBalance) {
        if (accountNumber == null || accountNumber.isBlank())
            throw new IllegalArgumentException("Account number is required.");
        if (ownerName == null || ownerName.isBlank())
            throw new IllegalArgumentException("Owner name is required.");
        if (openingBalance < 0)
            throw new IllegalArgumentException("Opening balance cannot be negative.");

        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = openingBalance;
    }

    // ---- read-only accessors ----
    public final String getAccountNumber() { return accountNumber; }
    public final String getOwnerName()     { return ownerName; }
    public final double getBalance()       { return balance; }

    // ---- abstract methods: each subclass defines its own behaviour ----
    public abstract String getAccountType();

    /** Rule hook: can this account type withdraw `amount` in total? */
    protected abstract boolean canWithdraw(double amount);

    // ---- shared, FINAL operations (same for every account) ----
    public final void deposit(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
    }

    /** Withdraws if the subclass rule allows it. Returns true on success. */
    public final boolean withdraw(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (!canWithdraw(amount))
            return false;                    // rule rejected it
        balance -= amount;
        return true;
    }

    /** Transfer = withdraw here + deposit there (atomic from the user's view). */
    public final boolean transfer(Account target, double amount) {
        if (target == null || target == this)
            throw new IllegalArgumentException("Invalid transfer target.");
        if (!withdraw(amount))
            return false;                    // nothing was taken, nothing sent
        target.deposit(amount);
        return true;
    }

    @Override
    public String toString() {
        return String.format("%-8s %-9s %-18s Balance: %10.2f",
                accountNumber, getAccountType(), ownerName, balance);
    }
}

/* ============================================================
 * 2) SavingsAccount  ->  earns interest, NO overdraft
 * ============================================================ */
class SavingsAccount extends Account {

    private final double interestRate;   // e.g. 0.04 = 4 %

    public SavingsAccount(String accountNumber, String ownerName,
                          double openingBalance, double interestRate) {
        super(accountNumber, ownerName, openingBalance);
        if (interestRate < 0)
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        this.interestRate = interestRate;
    }

    public double getInterestRate() { return interestRate; }

    @Override
    public String getAccountType() { return "SAVINGS"; }

    /** Savings rule: you may only withdraw money you actually have. */
    @Override
    protected boolean canWithdraw(double amount) {
        return amount <= getBalance();
    }

    /** Crediting interest is specific to savings accounts. */
    public double applyInterest() {
        double interest = getBalance() * interestRate;
        if (interest > 0) deposit(interest);
        return interest;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("  (rate %.2f%%)", interestRate * 100);
    }
}

/* ============================================================
 * 3) CheckingAccount  ->  overdraft allowed up to a limit
 * ============================================================ */
class CheckingAccount extends Account {

    private final double overdraftLimit;

    public CheckingAccount(String accountNumber, String ownerName,
                           double openingBalance, double overdraftLimit) {
        super(accountNumber, ownerName, openingBalance);
        if (overdraftLimit < 0)
            throw new IllegalArgumentException("Overdraft limit cannot be negative.");
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() { return overdraftLimit; }

    @Override
    public String getAccountType() { return "CHECKING"; }

    /** Checking rule: balance + overdraft limit is the spendable ceiling. */
    @Override
    protected boolean canWithdraw(double amount) {
        return amount <= getBalance() + overdraftLimit;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("  (overdraft %.2f)", overdraftLimit);
    }
}

/* ============================================================
 * 4) Bank  ->  owns the ArrayList<Account>
 * ============================================================ */
class Bank {

    private final List<Account> accounts = new ArrayList<>();   // <-- the ArrayList

    public void addAccount(Account account) {
        if (findAccount(account.getAccountNumber()) != null)
            throw new IllegalArgumentException(
                    "Account number already exists: " + account.getAccountNumber());
        accounts.add(account);
    }

    /** Returns the account or null. Case-insensitive. */
    public Account findAccount(String accountNumber) {
        for (Account a : accounts)
            if (a.getAccountNumber().equalsIgnoreCase(accountNumber))
                return a;
        return null;
    }

    /** Defensive copy so callers can't add/remove behind our back. */
    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    public double totalBalance() {
        double total = 0;
        for (Account a : accounts)     // polymorphic call to getBalance()
            total += a.getBalance();
        return total;
    }
}

/* ============================================================
 * 5) Menu-driven console UI
 * ============================================================ */
public class BankApp {

    private static final Scanner SC = new Scanner(System.in);
    private static final Bank BANK = new Bank();
    private static int accountSequence = 1001;

    public static void main(String[] args) {
        seedDemoData();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> createAccount();
                case 2 -> deposit();
                case 3 -> withdraw();
                case 4 -> transfer();
                case 5 -> showAccounts();
                case 6 -> applyInterestToSavings();
                case 0 -> running = false;
                default -> System.out.println("  ! Invalid option, try again.");
            }
        }
        System.out.println("\nGoodbye!");
        SC.close();
    }

    /* ---------------- menu ---------------- */

    private static void printMenu() {
        System.out.println("""

                ╔══════════════════════════════╗
                ║       JAVA BANK SYSTEM       ║
                ╠══════════════════════════════╣
                ║ 1) Create account            ║
                ║ 2) Deposit                   ║
                ║ 3) Withdraw                  ║
                ║ 4) Transfer                  ║
                ║ 5) List accounts / balance   ║
                ║ 6) Apply interest (savings)  ║
                ║ 0) Exit                      ║
                ╚══════════════════════════════╝""");
    }

    /* ---------------- actions ---------------- */

    private static void createAccount() {
        System.out.println("\n-- New Account --");
        System.out.println("  1) Savings   2) Checking");
        int type = readInt("Type: ");
        if (type != 1 && type != 2) {
            System.out.println("  ! Unknown account type.");
            return;
        }

        String owner = readLine("Owner name: ");
        double opening = readDouble("Opening balance: ");
        String number = "ACC" + accountSequence;

        try {
            Account account;
            if (type == 1) {
                double rate = readDouble("Interest rate (e.g. 0.04 for 4%): ");
                account = new SavingsAccount(number, owner, opening, rate);
            } else {
                double limit = readDouble("Overdraft limit: ");
                account = new CheckingAccount(number, owner, opening, limit);
            }
            BANK.addAccount(account);
            accountSequence++;                      // only consume the id on success
            System.out.printf("  ✓ Created %s account %s for %s%n",
                    account.getAccountType(), number, owner);
        } catch (IllegalArgumentException e) {
            System.out.println("  ! " + e.getMessage());
        }
    }

    private static void deposit() {
        System.out.println("\n-- Deposit --");
        Account a = askAccount("Account number: ");
        if (a == null) return;

        double amount = readDouble("Amount: ");
        try {
            a.deposit(amount);
            System.out.printf("  ✓ Deposited %.2f to %s. New balance: %.2f%n",
                    amount, a.getAccountNumber(), a.getBalance());
        } catch (IllegalArgumentException e) {
            System.out.println("  ! " + e.getMessage());
        }
    }

    private static void withdraw() {
        System.out.println("\n-- Withdraw --");
        Account a = askAccount("Account number: ");
        if (a == null) return;

        double amount = readDouble("Amount: ");
        try {
            if (a.withdraw(amount))
                System.out.printf("  ✓ Withdrew %.2f from %s. New balance: %.2f%n",
                        amount, a.getAccountNumber(), a.getBalance());
            else
                System.out.println("  ! Rejected: insufficient funds for this account type.");
        } catch (IllegalArgumentException e) {
            System.out.println("  ! " + e.getMessage());
        }
    }

    private static void transfer() {
        System.out.println("\n-- Transfer --");
        Account from = askAccount("From account: ");
        if (from == null) return;
        Account to = askAccount("To account:   ");
        if (to == null) return;
        if (from == to) {
            System.out.println("  ! Cannot transfer to the same account.");
            return;
        }

        double amount = readDouble("Amount: ");
        try {
            if (from.transfer(to, amount))
                System.out.printf("  ✓ Transferred %.2f from %s to %s%n",
                        amount, from.getAccountNumber(), to.getAccountNumber());
            else
                System.out.println("  ! Rejected: insufficient funds in the source account.");
        } catch (IllegalArgumentException e) {
            System.out.println("  ! " + e.getMessage());
        }
    }

    private static void showAccounts() {
        System.out.println("\n-- Accounts --");
        List<Account> all = BANK.getAccounts();
        if (all.isEmpty()) {
            System.out.println("  (no accounts yet)");
            return;
        }
        for (Account a : all)                 // polymorphism: each prints itself
            System.out.println("  " + a);
        System.out.printf("  %d account(s), total holdings: %.2f%n",
                all.size(), BANK.totalBalance());
    }

    private static void applyInterestToSavings() {
        System.out.println("\n-- Apply Interest (savings only) --");
        int count = 0;
        for (Account a : BANK.getAccounts()) {
            if (a instanceof SavingsAccount s) {      // polymorphic list, filtered by type
                double interest = s.applyInterest();
                System.out.printf("  %s: credited %.2f -> balance %.2f%n",
                        s.getAccountNumber(), interest, s.getBalance());
                count++;
            }
        }
        if (count == 0) System.out.println("  (no savings accounts)");
    }

    /* ---------------- input helpers ---------------- */

    private static Account askAccount(String prompt) {
        String number = readLine(prompt);
        Account a = BANK.findAccount(number);
        if (a == null) System.out.println("  ! No account found with number " + number);
        return a;
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!SC.hasNextLine()) {          // Ctrl+D / end of input
            System.out.println();
            System.exit(0);
        }
        return SC.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a valid number.");
            }
        }
    }

    /* ---------------- demo data ---------------- */

    private static void seedDemoData() {
        BANK.addAccount(new SavingsAccount("ACC1001", "Alice", 1500.00, 0.04));
        BANK.addAccount(new CheckingAccount("ACC1002", "Bob", 300.00, 500.00));
        accountSequence = 1003;
    }
}