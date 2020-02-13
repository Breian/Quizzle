import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GraphicInsertName extends  JFrame implements ActionListener {
    private JFrame frame;
    private JTextField textField;
    private JLabel WordPanel;
    protected String response;
    private boolean backClicked = false;

    public GraphicInsertName(String nomeComando){
        //CLASSE DEL FRAME USATO PER SCRIVERE IL NOME DELL'AMICO DA AGGIUNGERE O DELL'AMICO DA SFIDARE
        this.frame = new JFrame(nomeComando);
        this.frame.setPreferredSize(new Dimension(300,200));
        this.frame.setResizable(false);

        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        if(nomeComando.compareTo("aggiungi_amico") == 0){
            this.WordPanel = new JLabel("NOME AMICO DA AGGIUNGERE : ");
        }
        else {
            this.WordPanel = new JLabel("NOME AMICO DA SFIDARE : ");
        }
        JButton inviaButton = new JButton("INVIA");
        JButton backButton = new JButton("INDIETRO");
        backButton.setBackground(Color.BLUE);
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> {

            backClicked = true;
            frame.dispose();
        });
        inviaButton.setBackground(Color.BLACK);
        inviaButton.setForeground(Color.WHITE);
        inviaButton.addActionListener(this);
        this.textField = new JTextField();
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
        this.frame.add(WordPanel);
        this.frame.add(textField);
        this.frame.add(inviaButton);
        this.frame.add(backButton);
        this.frame.setLayout(new GridLayout(4,1));
        this.frame.pack();
        this.frame.repaint();
        this.frame.validate();


        //il frame appare centrale allo schermo
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        this.response = this.textField.getText();
        frame.dispose();
    }

    public String getResponse(){
        return this.response;
    }

    public boolean getBackClicked(){
        return this.backClicked;
    }
}