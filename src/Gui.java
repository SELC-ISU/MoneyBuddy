import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;


public class Gui extends JFrame implements ActionListener {
    private int need;
    private JLabel item1, item2;
    private JButton button;
    private JMenuBar bar;
    private JMenu file, help;
    private JMenuItem newItem, edit,close, extra,search;
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
        newItem = new JMenuItem("View Entries");
        edit = new JMenuItem("Edit Entries");
        close = new JMenuItem("Close");
        extra = new JMenu("Extra");
        search = new JMenuItem("Search");
        field = new JTextField(10);
        field2 = new JTextField(10);
        checkbox = new JCheckBox("Need?");
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
        file.add(newItem);
        file.add(edit);
        file.addSeparator();
        file.add(close);
        file.add(extra);
        help.add(search);
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
        dbEntriesPane = new JScrollPane(dbEntries,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // This is the container that provides the scrolling functionality
        dbEntriesPane.setPreferredSize(new Dimension(700, 425)); // This tells the scrolling box how big it should be
        entriesContainer.add(dbEntriesPane); // This adds the container to the content window

        //button action declaration
        search.addActionListener(this::actionPerformed);
        button.addActionListener(this);
        field.addActionListener(this);
    }
    @Override
    public void actionPerformed(ActionEvent e){
        String name = e.getActionCommand();
        //Search fuction in the help tab
        if(name.equals("Search")) {
            URI uri = null;
            try {
                uri = new URI("https://github.com/SELC-ISU/MoneyBuddy/blob/master/README.md");
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //Submit button action
        else if (name.equals("Submit")){

        }
        System.out.println("Hehe just pressed a button xD");
        amountInput = field.getText();
        memoInput = field2.getText();
        if (!(amountInput.equals("")) || !(memoInput.equals(""))){
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
            revalidate();
            repaint();
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

        dbEntries.setText(currentDatabase.getTransactions()); // Update the content pane with the latest database
        curBal.setText("Balance: $" + currentDatabase.getBal()); // Update the content pane with the latest balance
        revalidate();
        repaint();
    }


}