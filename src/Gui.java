import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;


public class Gui extends JFrame implements ActionListener {
    private JLabel item1, color;
    private JButton button, button2;
    private JMenuBar bar;
    private JMenu file, help;
    private JMenuItem newItem, edit,close, extra,hello,bye,search;

    public Gui(){
        super("Money Buddy");
        setLayout(new FlowLayout());
        item1 = new JLabel("This is a sentence");
        item1.setToolTipText("This is gonna show up on hover");

        color = new JLabel("This is a sentence");
        color.setOpaque(true);
        color.setBackground(Color.BLUE);
        button = new JButton("Click Me");

       bar = new JMenuBar();
       file = new JMenu("File");
       help = new JMenu("Help");
       newItem = new JMenuItem("New Entry");
       edit = new JMenuItem("Edit");
       close = new JMenuItem("Close");
       extra = new JMenu("Extra");
       hello = new JMenuItem("Hello");
       bye = new JMenuItem("Bye!");
       search = new JMenuItem("Search");

       bar.add(file);
       file.add(newItem);
       file.add(edit);
       file.addSeparator();
       file.add(close);
       file.add(extra);
       extra.add(hello);
       extra.add(bye);
       help.add(search);
       bar.add(help);

       setJMenuBar(bar);
       add(button);
       add(color);
        add(item1);

        search.addActionListener(this::actionPerformed);
        button.addActionListener(this);
    }
    @Override
    public void actionPerformed(ActionEvent e){
        String name = e.getActionCommand();
        if(name.equals("Search")) {
            URI uri = null;
            try {
                uri = new URI("https://www.google.com");
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else if (name.equals("Click Me")){

        }
        System.out.println("Hehe just pressed a button xD");

    }

}
