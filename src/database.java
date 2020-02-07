/**
 * Represents a database instance. This is the man in between everything else and the backend database.
 * @author Zachary Kline <www.ZachKline.us>
 */
public class database {
    String dbname;
    String dbpath;

    /**
     * Builds a new database object
     * @param name The name of the database; should be user-supplied
     */
    public database(String name) {
        dbname = name;
        dbpath = System.getProperty("user.home");
        dbpath = dbpath.concat("\\.MoneyBuddy\\" + name + "\\");
    }

    /**
     * Returns the SQL database path;
     * @return folder path of the SQL directory
     */
    public String getPath() {
        return dbpath;
    }
}
