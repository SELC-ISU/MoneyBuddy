import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

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
     * @param name The name of the database; should be user-supplied. If it exists, a connection will be made. If it does not exist, it will be created and initialized
     */
    public database(String name) {
        /* -- Sets up path variables -- */
        dbName = name;
        dbPath = System.getProperty("user.home"); //Will be used to store the absolute path of the db file
        dirPath = System.getProperty("user.home"); //Will be used to store the absolute path of the .MoneyBuddy folder
        File f; // Used to create a new folder if necessary

        if (System.getProperty("os.name").contains("Windows")) {
            dbPath = dbPath.concat("\\.MoneyBuddy\\" + name + ".db"); //Sets the path to use forward slashes for Windows file paths (the stupid way)
            dirPath = dirPath.concat("\\.MoneyBuddy\\");
            dbPath = "jdbc:sqlite:" + dbPath;
        } else if (System.getProperty("os.name").contains("Linux")) {
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
            e.printStackTrace();
        }

        try {
            statement.execute(); //Actually runs the prepared statement
        } catch (SQLException e) {
            System.out.println("Error initializing the 'transactions' table");
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
     * @param dateObj The date of the transaction
     * @param amount The monetary amount of the transaction; if you spend money, this should be negative. If you earned money, this should be positive
     * @param memo A note to tell the user what the transaction was for
     * @param need Binary; 1 if the transaction was a "need" or was necessary, or 0 if the transaction was a "want" or a pleasure expense. If amount is positive, this value will not be taken into account and will be entered as NULL in the database
     * @return error codes: 0 is okay, 1 is a date format error, 2 means that something was left blank (if amount > 0, "need" will not be parsed, but it must be filled), 3 means that something other than 0 or 1 was put into "need"
     */
    public int insertTransaction(LocalDate dateObj, double amount, String memo, int need) {
        String insertString = "INSERT INTO transactions(date,memo,amount,need) VALUES ('" + dateObj.toString() + "','" + memo + "'," + amount + "," + need + ")"; //Forms the table insert statement
        try {
            PreparedStatement stmt = dbcon.prepareStatement(insertString); //Formats the statement from the string
            stmt.execute(); //Runs the statement
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Mutator; Inserts a new transactions into the database under the current date
     * @param amount The monetary amount of the transaction; if you spend money, this should be negative. If you earned money, this should be positive
     * @param memo A note to tell the user what the transaction was for
     * @param need Binary; 1 if the transaction was a "need" or was necessary, or 0 if the transaction was a "want" or a pleasure expense. If amount is positive, this value will not be taken into account and will be entered as NULL in the database
     * @return error codes: 0 is okay, 2 means that something was left blank (if amount > 0, "need" will not be parsed, but it must be filled), 3 means that something other than 0 or 1 was put into "need"
     */
    public int insertTransaction(double amount, String memo, int need) {
        /* -- Sanity checks -- */
        if (memo.isBlank() || amount == 0) { //If memo or date has no meaningful characters (not counting whitespace), or amount is zero
            return 2;
        } else if (amount < 0 && !(need == 0 || need == 1)) { // If amount is negative (this is an expense) and need is not 0 or 1
            return 3;
        }
        /* -- End sanity checks -- */

        String insertString = "INSERT INTO transactions(date,memo,amount,need) VALUES (date('now'),'" + memo + "'," + amount + "," + need + ")"; //Forms the table insert statement
        try {
            PreparedStatement stmt = dbcon.prepareStatement(insertString); //Formats the statement from the string
            stmt.execute(); //Runs the statement
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Removes the transaction of a particular ID number.
     * @param id the ID number of the entry to remove from the database. Note that ID numbers may not line up perfectly with the row number, since previously deleted entries are not backfilled
     * @return 0 if entry was deleted successfully, otherwise returns -1 if there was an error (either database is read-only or doesn't exist somehow)
     */
    public int removeTransaction(int id) {
        try {
            PreparedStatement stmt = dbcon.prepareStatement("DELETE FROM transactions WHERE id=" + id);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    /**
     * Provides an ASCII-table formatted ArrayList of all the contents in the database
     * @return ArrayList of formatted database entries, starting with a header and a divider
     */
    public ArrayList getTransactions() {
        ArrayList<String> out = new ArrayList<String>();
        ResultSet rs;

        out.add("ID\t|\tDate\t\t|\tAmount\t|\tNecessity\t|\tMemo"); // Prepends header
        out.add("-------------------------------------------------------------------"); // Adds divider

        try {
            PreparedStatement stmt = dbcon.prepareStatement("SELECT id,date,amount,need,memo FROM transactions");
            rs = stmt.executeQuery();

            // Appends each entry to the ArrayList
            while (rs.next()) {
                String need = "unknown";

                // Classifies need/want
                if (rs.getInt("need") == 1) {
                    need = "need";
                } else if (rs.getInt("need") == 0) {
                    need = "want";
                }

                // Appends to ArrayList
                out.add(rs.getString("id") + "\t|\t" + rs.getString("date") + "\t|\t" + rs.getString("amount") + "\t|\t" + need + "\t\t|\t" + rs.getString("memo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }
}
