import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GraphicInit {
    private JFrame frame;
    private String buttonClicked;


    GraphicInit(){
        //CLASSE DEL FRAME INIZIALE DEL WORD QUIZZLE. CONTIENE I COMANDI "LOGIN" E "REGISTRA_UTENTE"
        this.frame = new JFrame();
        this.frame.setPreferredSize(new Dimension(240,280));
        this.frame.setResizable(false);
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JLabel imageQuizzle = new JLabel(new ImageIcon("./Quizzle_real.png"));
        JLabel welcomePanel = new JLabel("BENVENUTO! ACCEDI O REGISTRATI!");
        welcomePanel.setForeground(Color.BLACK);
        welcomePanel.setBackground(Color.WHITE);
        JButton regButton = new JButton("REGISTRA_UTENTE");
        regButton.setBackground(Color.BLACK);
        regButton.setForeground(Color.WHITE);
        regButton.addActionListener(e -> {
            this.buttonClicked = e.getActionCommand();
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
                else{
                frame.setVisible(true);
                }
            }
        });

        this.frame.setLayout(new GridLayout(4,1));
        this.frame.add(imageQuizzle);
        this.frame.add(welcomePanel);
        JButton logButton = new JButton("LOGIN");
        logButton.setBackground(Color.BLACK);
        logButton.setForeground(Color.WHITE);
        logButton.addActionListener(e -> {
            this.buttonClicked = e.getActionCommand();
            frame.dispose();
        });
        this.frame.add(welcomePanel);
        this.frame.add(regButton);
        this.frame.add(logButton);



        this.frame.pack();
        this.frame.validate();

        //il frame appare centrale allo schermo
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

    }

    protected String getButtonClicked(){
        return this.buttonClicked;
    }
}