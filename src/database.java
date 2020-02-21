import java.io.File;
import java.sql.*;

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
                    "CREATE TABLE IF NOT EXISTS transactions(id INT AUTO_INCREMENT PRIMARY_KEY, date DATE NOT NULL, amount FLOAT NOT NULL, memo VARCHAR(255), need TINYINT NOT NULL)"
            );
        } catch (SQLException e) {
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
}
