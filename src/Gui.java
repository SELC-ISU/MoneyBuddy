import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;


public class Gui extends JFrame implements ActionListener {
    private int need;
    private JLabel item1, item2;
    private JButton button;
    private JMenuBar bar;
    private JMenu file, help, checkbooks;
    private JMenuItem edit, close, github, newCheckbook, rmCheckbook, stats;
    private Container amount,  memo, date, check, entriesContainer;
    private JTextField field, field2;
    private JLabel dbEntries;
    private JScrollPane dbEntriesPane;
    private JLabel curBal;
    private SpinnerModel model;
    private JSpinner spinner;
    private String amountInput,  memoInput;
    private Date dateInput;
    private JCheckBox checkbox;
    private database currentDatabase;

    public Gui(){
        //Declaring Panes
        super("Money Buddy");
        setLayout(new FlowLayout());
        item1 = new JLabel("Amount: ");
        amount = getContentPane();
        memo = getContentPane();
        date = getContentPane();
        check = getContentPane();
        entriesContainer = getContentPane();
        //Icon
        this.setIconImage(new ImageIcon("media/icon-apple-flyingmoney.png").getImage());
        //declaring new objects
        item2 = new JLabel("Memo: ");
        button = new JButton("Submit");
        bar = new JMenuBar();
        file = new JMenu("File");
        help = new JMenu("Help");
        edit = new JMenuItem("Remove entry");
        close = new JMenuItem("Exit");
        checkbooks = new JMenu("Checkbooks");
        github = new JMenuItem("Github");
        stats =  new JMenuItem("Statistics");
        field = new JTextField(10);
        field2 = new JTextField(10);
        checkbox = new JCheckBox("Need?");
        newCheckbook = new JMenuItem("New checkbook");
        rmCheckbook = new JMenuItem("Delete checkbook");
        //declaring for dates
        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        Date latestDate = calendar.getTime();
        model = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR);
        //declaration for spinner to work with dates
        spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner,"yyyy-MM-dd"));
        //declaration of the header menu (forgetting the name of it)
        bar.add(file);
        file.add(stats);
        file.add(edit);
        file.addSeparator();
        file.add(checkbooks);
        refreshCheckbooks();
        file.add(close);
        help.add(github);
        bar.add(help);
        //Adding certain objects to certain panes
        add(item1);
        amount.add(field, BorderLayout.SOUTH);
        amount.add(button);
        add(item2);
        memo.add(field2, BorderLayout.SOUTH);
        memo.add(button);
        date.add(spinner);
        check.add(checkbox);
        setResizable(false); // Let's keep things simple

        setJMenuBar(bar);

        currentDatabase = new database("default"); // Defines the current database. This should be changeable from the "File" tab with help from dbList() in helpers.java

        /* Sets up the divider to print the balance behind */
        JSeparator balSeparator = new JSeparator(SwingConstants.VERTICAL);
        Dimension balSeparatorDimensions = balSeparator.getPreferredSize();
        balSeparatorDimensions.height = 50;
        balSeparator.setPreferredSize(balSeparatorDimensions);
        entriesContainer.add(balSeparator);

        /* Prints the balance in the header */
        curBal = new JLabel("Balance: $" + currentDatabase.getBal()); // Prepares the balance entry
        entriesContainer.add(curBal); // This is automatically updated whenever AddEntry is called

        /* Print the database in a JScrollPane */
        dbEntries = new JLabel(currentDatabase.getTransactions()); // This is the the label that is kept updated
        dbEntries.setVerticalAlignment(JLabel.TOP);
        dbEntriesPane = new JScrollPane(dbEntries,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // This is the container that provides the scrolling functionality
        dbEntriesPane.setPreferredSize(new Dimension(700, 425)); // This tells the scrolling box how big it should be
        entriesContainer.add(dbEntriesPane); // This adds the container to the content window

        //button action declaration
        github.addActionListener(this);
        button.addActionListener(this);
        field.addActionListener(this);
        close.addActionListener(this);
        newCheckbook.addActionListener(this);
        rmCheckbook.addActionListener(this);
        edit.addActionListener(this);
        stats.addActionListener(this);
    }
    @Override
    public void actionPerformed(ActionEvent event){
        String name = event.getActionCommand();
        String[] dbList = helpers.dbList(); // Used for the checkbooks buttons

        if(name.equals("Github")) {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/SELC-ISU/MoneyBuddy/blob/master/README.md"));
            } catch (IOException | URISyntaxException err) {
                err.printStackTrace();
            }
        } else if (name.equals("Exit")) {
            this.dispose();
        } else if (name.equals("Submit")) {
            amountInput = field.getText();
            memoInput = field2.getText();

            if (!amountInput.equals("") && !memoInput.equals("")) { // We were going to use .isBlank(), but turns out that's literally a Java 11 function and we otherwise have Java 8 compatibility
                System.out.println(amountInput + " is what is in the amount text box.");
                field.setText("");
                System.out.println(memoInput + " is what is in the memo text box");
                field2.setText("");
                dateInput = (Date)spinner.getValue();
                System.out.println(dateInput);
                if(checkbox.isSelected()){
                    need = 1;
                    checkbox.setSelected(false);
                } else {
                    need = 0;
                }
                System.out.println(need);
                AddEntry(amountInput,  memoInput, dateInput, need);
            }
        } else if (helpers.doesArrayContain(dbList, name)) { // If this is a checkbook (if the name of the button matches any from the list of databases)
            currentDatabase = new database(name); // Reestablish the current database
            refreshDataframe();
        } else if (name.equals("New checkbook")) {
            String nameOfNewDatabase = JOptionPane.showInputDialog(this, "What do you want to name this new database?", null);
            if (nameOfNewDatabase != null && !nameOfNewDatabase.equals("")) {
                currentDatabase = new database(nameOfNewDatabase);
                refreshDataframe();
                refreshCheckbooks();
            }
        } else if (name.equals("Remove entry")) {
            String entryToRemoveString = JOptionPane.showInputDialog(this, "What is the ID number of the entry you want to remove?", null);

            if (entryToRemoveString != null && !entryToRemoveString.equals("")) {
                int entryToRemoveInt = Integer.parseInt(entryToRemoveString);
                currentDatabase.removeTransaction(entryToRemoveInt);
                refreshDataframe();
            }
        } else if (name.equals("Delete checkbook")) {
            String checkbookToRemove = JOptionPane.showInputDialog(this, "What is the exact name of the checkbook you wish to remove? (CASE SENSITIVE)", null);

            if (checkbookToRemove != null && !checkbookToRemove.equals("")) {
                int areYouSureAboutThat = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete \"" + checkbookToRemove + "\"? This is a permanent action and cannot be undone!", "Delete?",  JOptionPane.YES_NO_OPTION);

                if (areYouSureAboutThat == JOptionPane.YES_OPTION) {
                    File f = new File(System.getProperty("user.home") + "/.MoneyBuddy/" + checkbookToRemove + ".db");
                    f.delete();
                    refreshCheckbooks();
                    System.out.println("Checkbook deleted");
                }
            }
        } else if (name.equals("Stats")){
        //database.getStatistics()
            JOptionPane.showMessageDialog( null, currentDatabase.getStatistics() , "Stats", 0);
        }
        else {
            System.out.println("Button was pressed, but no function was assigned.");
        }
    }

    /**
     * Inserts a transaction into the database
     * @param amount The amount of the transactions: if an expense, this should be negative
     * @param memo The memo to associate with the transaction; this should be a string describing the transaction
     * @param date The date the transaction took place
     * @param need Whether the transaction was a "need" (need=1) or a "want" (need=0). If the transaction amount is positive, this value is ignored
     */
    public void AddEntry(String amount, String memo, Date date, int need) {
        currentDatabase.insertTransaction(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), Double.parseDouble(amount), memo, need); // Feeds input into the database, converting deprecated Date object into LocalDate

        refreshDataframe();
    }

    /**
     * Just a handy little macro that refreshes the database and balance display
     */
    public void refreshDataframe() {
        dbEntries.setText(currentDatabase.getTransactions()); // Update the content pane with the latest database
        curBal.setText("Balance: $" + currentDatabase.getBal()); // Update the content pane with the latest balance
        revalidate();
        repaint();
    }

    /**
     * Another handy function to refresh the JMenu checkbooks to show all active databases
     */
    public void refreshCheckbooks() {
        checkbooks.removeAll();

        checkbooks.add(newCheckbook);
        checkbooks.add(rmCheckbook);
        checkbooks.addSeparator();

        String[] dbList = helpers.dbList();
        for (int i = 0; i < dbList.length; i++) {
            JMenuItem checkbook = new JMenuItem(dbList[i]);
            checkbooks.add(checkbook);
            checkbook.addActionListener(this);
        }
    }
}