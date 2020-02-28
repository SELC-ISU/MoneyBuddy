import java.io.File;
import java.sql.*;
import java.util.Scanner;

/**
 * Represents a database instance. This is the man in between everything else and the backend database, implementing in JDBC.
 * @author Zachary Kline <www.ZachKline.us>
 */
public class database {
    private String dbName;
    private String dbPath;
    private String dirPath;
    private PreparedStatement statement;
    private Connection dbcon;
    static final String USER = "moneybuddy";
    static final String PASS = "moneybuddy";

    /**
     * Constructor; Builds a new database object
     * @param name The name of the database; should be user-supplied
     */
    public database(String name) {
        /* -- Sets up path variables -- */
        dbName = name;
        dbPath = System.getProperty("user.home");
        dirPath = System.getProperty("user.home");
        File f; // Used to create a new folder if necessary

        if (System.getProperty("os.name").equals("Windows")) {
            dbPath = dbPath.concat("\\.MoneyBuddy\\" + name + ".db"); //Sets the path to use forward slashes for Windows file paths
            dirPath = dirPath.concat("\\.MoneyBuddy\\");
            dbPath = "jdbc:sqlite:" + dbPath;
        } else if (System.getProperty("os.name").equals("Linux")) {
            dbPath = dbPath.concat("/.MoneyBuddy/" + name + ".db"); //Sets the path to use backslashes for Linux file paths (the better way)
            dirPath = dirPath.concat("/.MoneyBuddy/");
            dbPath = "jdbc:sqlite:" + dbPath;
        }

        /* -- Create directory if it doesn't already exist -- */
        f = new File(dirPath);
        f.mkdir();
        /* -- End creating directory -- */

        /* -- Sets up database connection -- */
        dbcon = null;
        try {
            dbcon = DriverManager.getConnection(dbPath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /* -- End setting up database connection -- */

        /* -- If this is a new database, initializes the database structure. Else, does nothing -- */
        try {
            statement = dbcon.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS transactions(id INTEGER PRIMARY KEY AUTOINCREMENT, date DATE NOT NULL, amount FLOAT NOT NULL, memo VARCHAR(255), need TINYINT)"
            );
        } catch (SQLException e) {
            System.out.println("Error initializing the 'transactions' table");
            e.printStackTrace();
        }

        try {
            statement.execute(); //Actually runs the prepared statement
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /* -- End setting up database structure -- */
    }

    /**
     * Accessor; Returns the SQL database path;
     * @return path of the SQLite DB
     */
    public String getDbPath() {
        return dbPath;
    }

    /**
     * Accessor; Returns the program data path
     * @return folder path of the program data; where everything persistent is stored
     */
    public String getDirPath() {
        return dirPath;
    }

    /**
     * Accessor; Returns database name
     * @return name of the active database
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * Accessor; Returns the running balance of the current active transactions database
     * @return the sum of all transactions (double), which should be the running account balance
     */
    public double getBal() {
        ResultSet rs = null;
        try {
            PreparedStatement stmt = dbcon.prepareStatement("SELECT sum(amount) FROM transactions");
            rs = stmt.executeQuery();
            return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Mutator; Inserts a new transactions into the database
     * @param date The date of the transaction in ISO8601 format (YYYY-MM-DD). If they are entering the date as today, you can enter "today" or "now" or "curdate". These keywords will enumerate to the current date
     * @param amount The monetary amount of the transaction; if you spend money, this should be negative. If you earned money, this should be positive
     * @param memo A note to tell the user what the transaction was for
     * @param need Binary; 1 if the transaction was a "need" or was necessary, or 0 if the transaction was a "want" or a pleasure expense. If amount is positive, this value will not be taken into account and will be entered as NULL in the database
     * @return error codes: 0 is okay, 1 is a date format error, 2 means that something was left blank (if amount > 0, "need" will not be parsed, but it must be filled), 3 means that something other than 0 or 1 was put into "need"
     */
    public int insertTransaction(String date, double amount, String memo, int need) {
        /* -- Sanity checks -- */
        if (memo.isBlank() || date.isBlank() || amount == 0) { //If memo or date has no meaningful characters (not counting whitespace), or amount is zero
            return 2;
        } else if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) { //Regex; If it matches the format NNNN-NN-NN (N = some number)
            return 1;
        } else if (amount < 0 && !(need == 0 || need == 1)) { // If amount is negative (this is an expense) and need is not 0 or 1
            return 3;
        }

        Scanner scan = new Scanner(date).useDelimiter("-");

        int year = scan.nextInt();
        int month = scan.nextInt();
        int day = scan.nextInt();

        if (year < 0 || month > 12 || month <= 0 || day <= 0 || day > 31) { //If year is negative, month isn't between 1 and 12, or day isn't between 1 and 31, error
            return 1;
        }
        /* -- End sanity checks -- */

        String insertString = "INSERT INTO transactions(date,memo,amount,need) VALUES ('" + date + "','" + memo + "'," + amount + "," + need + ")"; //Forms the table insert statement
        try {
            PreparedStatement stmt = dbcon.prepareStatement(insertString);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
