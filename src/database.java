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

        dbPath = dbPath.concat("/.MoneyBuddy/" + name + ".db");
        dirPath = dirPath.concat("/.MoneyBuddy/");
        dbPath = "jdbc:sqlite:" + dbPath;

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

            /* Utilizes PreparedStatement to cleanse user input of potentially malicious patterns */
            PreparedStatement stmt = dbcon.prepareStatement("INSERT INTO transactions(date,memo,amount,need) VALUES (?,?,?,?)");
            stmt.setString(1, dateObj.toString());
            stmt.setString(2, memo);
            stmt.setDouble(3, amount);
            stmt.setInt(4, need);

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

            PreparedStatement stmt = dbcon.prepareStatement("DELETE FROM transactions WHERE id=?");
            stmt.setInt(1, id);
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
                String amountPrefix = "<p style='color:#000000'>$"; // This color will be replaced in a few lines...
                String amountSuffix = "</p>";

                // Classifies need/want and sets amountPrefix color
                if (rs.getInt("need") == -1) {
                    need = "N/A";
                    amountPrefix = "<p style='color:#228b22'>$"; // Sets color to forest green because this was income
                } else if (rs.getInt("need") == 1) {
                    need = "need";
                    amountPrefix = "<p style='color:#b22222'>$"; // Sets color to red because this was a withdraw
                } else if (rs.getInt("need") == 0) {
                    need = "want";
                    amountPrefix = "<p style='color:#b22222'>$"; // Sets color to red because this was a withdraw
                }

                entries.add("<tr><td><b style='color:#1e90ff;'>" + rs.getString("id") + "</b></td><td>" + rs.getString("date") + "</td><td>" + need + "</td><td>" + amountPrefix + rs.getString("amount") + amountSuffix + "</td><td>" + rs.getString("memo") + "</td></tr>");
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
        boolean areThereAnyWithdraws = true;

        /* Retrieve results from the database */
        try {
            dbcon = DriverManager.getConnection(dbPath);

            /**
             * Okay so I actually ran into 2 issues with this statement here. Let me explain:
             *
             * For starters, the "-1 day" thing. For some reason, SQLite refuses to acknowledge
             * the first day of the month, even with the ">=". I finally got frustrated enough
             * to just pad it by a day so that it would be forced to look at the first day. While
             * counterintuitive, based on my testing, this patch *does not* include the last day
             * of the previous month. Why? Who knows.
             *
             * Okay, thing #2: the timeframe this statement selects is not just "this month," it's
             * actually "from this month onward." Why? Well, I'm going to point fingers at SQLite here.
             * Based on this page:
             *  - https://www.techonthenet.com/sqlite/functions/datetime.php
             * the datetime() function in SQLite does not support anything to specify "the end of the month"
             * and for our SE186X demo (to be performed May 1 2020), I created a demo database filled with
             * transactions all throughout May. If I selected the timeframe "from this month to the current
             * time," which is the next logical step down, I wouldn't have been able to showcase the statistics
             * feature. So this patch, while it shouldn't *really* create any issues, technically invalidates
             * the assumption of the timeframe it selects.
             */
            PreparedStatement stmt = dbcon.prepareStatement("SELECT id,date,amount,need,memo FROM transactions WHERE date >= datetime('now', 'start of month', '-1 day')");
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

            /* Check to see if there were any withdraws. If there were, calculate percentages. If not, set flag.
            The flag will be checked later, and if it's false, percentages won't be displayed */
            if (totalNeeds + totalWants !=  0) {
                percNeed = ((double) totalNeeds / (totalNeeds + totalWants)) * 100;
                percWant = ((double) totalWants / (totalNeeds + totalWants)) * 100;
            } else {
                areThereAnyWithdraws = false;
            }

            dbcon.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Unknown error parsing the database results.";
        }

        String colorOfDelta = "";
        if (spentThisMonth + earnedThisMonth > 0) {
            colorOfDelta = "green";
        } else if (spentThisMonth + earnedThisMonth < 0) {
            colorOfDelta = "red";
        } else {
            colorOfDelta = "black";
        }

        /* Format statistics into HTML. I rounded everything to 2 decimal places */
        returnStringHTML = returnStringHTML.concat(
                "<html><body><h1>Month-to-date statistics</h1><h3>Checkbook: " + dbName + "</h3><br><table>" + // Header
                "<tr><td><b>Current Balance</b>:</td><td>$" + Math.round(currentBalance * 100.0) / 100.0 + "</td></tr>" +
                "<tr><td><b>Spent this month</b>:</td><td>$" + Math.abs(Math.round(spentThisMonth * 100.0) / 100.0) + "</td></tr>" +
                "<tr><td><b>Earned this month</b>:</td><td>$" + Math.round(earnedThisMonth * 100.0) / 100.0 + "</td></tr>" +
                "<tr><td><b color='" + colorOfDelta + "'>Delta</b>:</td><td>$" + Math.round((spentThisMonth + earnedThisMonth) * 100.0) / 100.0 + "</td></tr>");
        if (areThereAnyWithdraws) {
            returnStringHTML = returnStringHTML.concat("<tr></tr><tr><td><b>Percentage of Needs</b>:</td><td>" + Math.round(percNeed * 100.0) / 100.0 + "%</td></tr>");
            returnStringHTML = returnStringHTML.concat("<tr><td><b>Percentage of Wants</b>:</td><td>" + Math.round(percWant * 100.0) / 100.0 + "%</td></tr>");
        }

        returnStringHTML = returnStringHTML.concat("</table></body></html>"); // Footer

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

            PreparedStatement stmt = dbcon.prepareStatement("SELECT id FROM transactions WHERE id=?");
            stmt.setInt(1, id);
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
