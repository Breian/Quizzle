import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GraphicRegLogin {
    private JFrame frame;
    private JTextField textUser;
    private JPasswordField pwUser;
    private JLabel UPanel;
    private JLabel Ppanel;
    private String username;
    protected char[] password;
    protected boolean backClicked = false;

    GraphicRegLogin(String command){
        //CLASSE DEL FRAME PER LA REGISTRAZIONE O IL LOGIN DELL'UTENTE
        this.frame = new JFrame();
        this.frame.setPreferredSize(new Dimension(250,200));
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.setResizable(false);
        if(command.compareTo("REGISTRA_UTENTE") == 0) {
            this.UPanel = new JLabel("USERNAME (ALMENO 3 CARATTERI) : ");
        }
        else{
            this.UPanel = new JLabel("USERNAME : ");
        }
        this.Ppanel = new JLabel("PASSWORD : ");
        JButton inviaButton = new JButton("INVIA");
        inviaButton.setBackground(Color.BLACK);
        inviaButton.setForeground(Color.WHITE);
        JButton backButton = new JButton("INDIETRO");
        backButton.setBackground(Color.BLUE);
        backButton.setForeground(Color.WHITE);

        inviaButton.addActionListener(e -> {
            username = textUser.getText();
            password = pwUser.getPassword();
            frame.dispose();
        });
        backButton.addActionListener(e -> {
            backClicked = true;
            frame.dispose();
        });
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(JOptionPane.showConfirmDialog(frame,
                        "Sei sicuro di voler uscire? Se si, uscirai dal gioco!", "ATTENZIONE",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    System.exit(0);
                }
            }
        });
        this.textUser = new JTextField();
        this.pwUser = new JPasswordField();
        this.frame.setLayout(new GridLayout(6,1));
        this.frame.add(UPanel);
        this.frame.add(textUser);
        this.frame.add(Ppanel);
        this.frame.add(pwUser);
        this.frame.add(inviaButton);
        this.frame.add(backButton);

        this.frame.pack();
        this.frame.validate();

        //il frame appare centrale allo schermo
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

    }

    protected String getUsername(){
        return this.username;
    }

    protected char[] getPassword(){
        return this.password;
    }

    protected boolean getBackClicked(){
        return this.backClicked;
    }
}