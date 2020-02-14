import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Represents a database instance. This is the man in between everything else and the backend database, implementing in JDBC.
 * @author Zachary Kline <www.ZachKline.us>
 */
public class database {
    private String dbname;
    private String dbpath;
    static final String USER = "moneybuddy";
    static final String PASS = "moneybuddy";

    /**
     * Constructor; Builds a new database object
     * @param name The name of the database; should be user-supplied
     */
    public database(String name) {
        /* -- Sets up path variables -- */
        dbname = name;
        dbpath = System.getProperty("user.home");

        if (System.getProperty("os.name").equals("Windows")) {
            dbpath = dbpath.concat("\\.MoneyBuddy\\" + name + ".db"); //Sets the path to use forward slashes for Windows file paths
            dbpath = "jdbc:sqlite:" + dbpath;
        } else if (System.getProperty("os.name").equals("Linux")) {
            dbpath = dbpath.concat("/.MoneyBuddy/" + name + ".db"); //Sets the path to use backslashes for Linux file paths (the better way)
            dbpath = "jdbc:sqlite:" + dbpath;
        }
        /* -- End setting up path variable -- */

        //FIXME - Make sure you create ~/.MoneyBuddy/ with mkdir() if it doesn't exist

        /* -- Sets up database connection -- */
        try (Connection con = DriverManager.getConnection(dbpath)) { //Tries to establish a connection to the database
            System.out.println("The driver name is " + con.getMetaData().getDriverName());
            System.out.println("A new database has been created.");
        } catch (SQLException e) { //If connection to database is unsuccessful, print error
            System.out.println(e.getMessage());
        }
        /* -- End setting up database connection -- */
    }

    /**
     * Accessor; Returns the SQL database path;
     * @return folder path of the SQL directory
     */
    public String getPath() {
        return dbpath;
    }
}
