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
     * Accessor; Returns the running balance of the current active transactions database rounded to 2 decimal places
     * @return the sum of all transactions (double), which should be the running account balance
     */
    public double getBal() {
        ResultSet rs = null;
        try {
            dbcon = DriverManager.getConnection(dbPath);

            PreparedStatement stmt = dbcon.prepareStatement("SELECT sum(amount) FROM transactions");
            rs = stmt.executeQuery();

            double sum = rs.getDouble(1);

            dbcon.close();

            return Math.round(sum * 100.0) / 100.0;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
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

            /* If this entry is income as opposed to an expense, set need to -1 so that it can be handled later as N/A */
            if (amount > 0) {
                need = -1;
            }

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
        if (!doesEntryExist(id)) {
            return -1;
        }

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
        ResultSet rs;

        /* Append database entries as HTML table rows */
        ArrayList<String> entries = new ArrayList<>(); // Used for sorting
        try {
            dbcon = DriverManager.getConnection(dbPath);

            PreparedStatement stmt = dbcon.prepareStatement("SELECT id,date,amount,need,memo FROM transactions");
            rs = stmt.executeQuery();

            while (rs.next()) {
                String need = "unknown";

                // Classifies need/want
                if (rs.getInt("need") == -1) {
                    need = "N/A";
                } else if (rs.getInt("need") == 1) {
                    need = "need";
                } else if (rs.getInt("need") == 0) {
                    need = "want";
                }

                entries.add("<tr><td>" + rs.getString("id") + "</td><td>" + rs.getString("date") + "</td><td>" + need + "</td><td>$" + rs.getString("amount") + "</td><td>" + rs.getString("memo") + "</td></tr>");
            }

            dbcon.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /* Provides HTML headers */
        String output = "";
        output = output.concat("<html><body><table style='width:100%'>" +
                "<tr>" +
                "<th>~ID~</th>" +
                "<th>~~~Date~~~</th>" +
                "<th>~~~Necessity~~~</th>" +
                "<th>~~~Amount~~~</th>" +
                "<th>~~~Memo~~~</th></tr>");

        /* Adds entries to the table in reverse order so that latest entries are at the top */
        for (int i = entries.size() - 1; i >= 0; i--) {
            output = output.concat(entries.get(i));
        }

        /* Provides HTML footers */
        output = output.concat("</table></body></html>");

        return output;
    }

    /**
     * Returns an HTML table for related statistics from the current database
     * @return an HTML table for related statistics from the current database
     */
    public String getStatistics() {
        ResultSet rs;
        String returnStringHTML = "";
        double percNeed = 0;
        double percWant = 0;
        double currentBalance = this.getBal();
        double spentThisMonth = 0;
        double earnedThisMonth = 0;

        /* Retrieve results from the database */
        try {
            dbcon = DriverManager.getConnection(dbPath);

            PreparedStatement stmt = dbcon.prepareStatement("SELECT id,date,amount,need,memo FROM transactions WHERE date BETWEEN datetime('now', 'start of month') AND datetime('now', 'localtime')");
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Unknown error connecting to the database.";
        }

        /* Parses statistics */
        try {
            int totalNeeds = 0;
            int totalWants = 0;

            while (rs.next()) {
                if (rs.getInt("need") == 1) {
                    totalNeeds++;
                } else if (rs.getInt("need") == 0) {
                    totalWants++;
                }

                if (rs.getInt("need") != -1) { // If this was an expense
                    spentThisMonth += rs.getDouble("amount"); // Assumed negative
                } else {
                    earnedThisMonth += rs.getDouble("amount"); // Assumed positive
                }
            }

            percNeed = ((double) totalNeeds / (totalNeeds + totalWants)) * 100;
            percWant = ((double) totalWants / (totalNeeds + totalWants)) * 100;

            dbcon.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Unknown error parsing the database results.";
        }

        /* Format statistics into HTML. I rounded everything to 2 decimal places */
        returnStringHTML = returnStringHTML.concat(
                "<html><body><h1>Month-to-date statistics</h1><br><table>" + // Header
                "<tr><td><b>Current Balance</b>:</td><td>$" + Math.round(currentBalance * 100.0) / 100.0 + "</td></tr>" +
                "<tr><td><b>Spent this month</b>:</td><td>$" + Math.abs(Math.round(spentThisMonth * 100.0) / 100.0) + "</td></tr>" +
                "<tr><td><b>Earned this month</b>:</td><td>$" + Math.round(earnedThisMonth * 100.0) / 100.0 + "</td></tr>" +
                "<tr><td><b>Delta</b>:</td><td>$" + Math.round((spentThisMonth + earnedThisMonth) * 100.0) / 100.0 + "</td></tr><tr></tr>" +
                "<tr><td><b>Percentage of Needs</b>:</td><td>" + Math.round(percNeed * 100.0) / 100.0 + "%</td></tr>" +
                "<tr><td><b>Percentage of Wants</b>:</td><td>" + Math.round(percWant * 100.0) / 100.0 + "%</td></tr>" +
                "</table></body></html>"); // Footer

        return returnStringHTML;
    }

    /**
     * Returns boolean whether or not the entry with the given ID exists in this database
     * @param id the ID of the transaction to check for
     * @return whether or not the transaction of this the id exists
     */
    private boolean doesEntryExist(int id) {
        ResultSet rs;

        try {
            dbcon = DriverManager.getConnection(dbPath);

            PreparedStatement stmt = dbcon.prepareStatement("SELECT id FROM transactions WHERE id=" + id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                dbcon.close();
                return true;
            } else {
                dbcon.close();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
