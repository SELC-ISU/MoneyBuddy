import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;


public class Gui extends JFrame implements ActionListener {
    private int need;
    private JLabel item1, item2, item3;
    private JButton button;
    private JMenuBar bar;
    private JMenu file, help;
    private JMenuItem newItem, edit,close, extra,search;
    private Container amount,  memo, date, check;
    private JTextField field, field2;
    private SpinnerModel model;
    private JSpinner spinner;
    private JFormattedTextField ftf;
    private String amountInput,  memoInput;
    private Date dateInput;
    private ArrayList<JLabel> label;
    private JCheckBox checkbox;

    public Gui(){
        super("Money Buddy");
        setLayout(new FlowLayout());
        item1 = new JLabel("Amount: ");
        amount = getContentPane();
        memo = getContentPane();
        date = getContentPane();
        check = getContentPane();

        this.setIconImage(new ImageIcon("media/icon-apple-flyingmoney.png").getImage());

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

        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        Date latestDate = calendar.getTime();
        model = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR);

        spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner,"yyyy-MM-dd"));

        bar.add(file);
        file.add(newItem);
        file.add(edit);
        file.addSeparator();
        file.add(close);
        file.add(extra);
        help.add(search);
        bar.add(help);

        add(item1);
        amount.add(field, BorderLayout.SOUTH);
        amount.add(button);
        add(item2);
        memo.add(field2, BorderLayout.SOUTH);
        memo.add(button);
        date.add(spinner);
        check.add(checkbox);

        setJMenuBar(bar);


        search.addActionListener(this::actionPerformed);
        button.addActionListener(this);
        field.addActionListener(this);
    }
    @Override
    public void actionPerformed(ActionEvent e){
        String name = e.getActionCommand();
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
        }
    }
    public static void AddEntry(String amount, String memo, Date date, int need) {
        database UserDB = new database("UserDB"); // Ideally in the future, we will allow users to switch between databases to manage different checkbooks. For now, we'll just have 1 database called "UserDB"
        UserDB.insertTransaction(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), Double.parseDouble(amount), memo, need); // Feeds input into the database, converting deprecated Date object into LocalDate
    }

}
