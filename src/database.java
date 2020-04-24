import java.io.File;
import java.sql.*;
import java.time.LocalDate;

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

        try {
            dbcon.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        try {
            dbcon = DriverManager.getConnection(dbPath);

            String insertString = "INSERT INTO transactions(date,memo,amount,need) VALUES ('" + dateObj.toString() + "','" + memo + "'," + amount + "," + need + ")"; //Forms the table insert statement
            PreparedStatement stmt = dbcon.prepareStatement(insertString); //Formats the statement from the string

            stmt.execute(); //Runs the statement

            dbcon.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Mutator; Removes the transaction of a particular ID number.
     * @param id the ID number of the entry to remove from the database. Note that ID numbers may not line up perfectly with the row number, since previously deleted entries are not backfilled
     * @return 0 if entry was deleted successfully, otherwise returns -1 if there was an error (either database is read-only or doesn't exist somehow)
     */
    public int removeTransaction(int id) {
        try {
            dbcon = DriverManager.getConnection(dbPath);

            PreparedStatement stmt = dbcon.prepareStatement("DELETE FROM transactions WHERE id=" + id);
            stmt.execute();

            dbcon.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    /**
     * Accessor; Provides an HTML table representation of the database
     * @return HTML table String
     */
    public String getTransactions() {
        String output = "";
        ResultSet rs;

        /* Provide HTML table headers */
        output = output.concat("<html><body><table style='width:100%'>" +
                "<tr>" +
                "<th>ID</th>" +
                "<th>Date</th>" +
                "<th>Amount</th>" +
                "<th>Necessity</th>" +
                "<th>Memo</th></tr>");

        /* Append database entries as HTML table rows */
        try {
            dbcon = DriverManager.getConnection(dbPath);

            PreparedStatement stmt = dbcon.prepareStatement("SELECT id,date,amount,need,memo FROM transactions");
            rs = stmt.executeQuery();

            while (rs.next()) {
                String need = "unknown";

                // Classifies need/want
                if (rs.getInt("need") == 1) {
                    need = "need";
                } else if (rs.getInt("need") == 0) {
                    need = "want";
                }

                output = output.concat("<tr><td>" + rs.getString("id") + "</td><td>" + rs.getString("date") + "</td><td>" + rs.getString("amount") + "</td><td>" + need + "</td><td>" + rs.getString("memo") + "</td></tr>");
            }

            dbcon.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        output = output.concat("</table></body></html>");

        return output;
    }
}
