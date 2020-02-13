import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GraphicInterfaceSfida extends JFrame {
    private JFrame frame;
    private JLabel welcomePanel;
    private String inputUtente = null;
    protected boolean closed = false;
    public GraphicInterfaceSfida(String utente, String sfidante){
        //CLASSE DEL FRAME DI ACCETTAZIONE SFIDA
        this.frame = new JFrame("Thread di " + utente);


        this.frame.setPreferredSize(new Dimension(400,220));
        this.frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.frame.setResizable(false);
        this.welcomePanel = new JLabel("Benvenuto nella sfida di Word Quizzle!");
        JLabel stringSfida = new JLabel(sfidante + " ti ha sfidato! Accetti la sfida? Premi si oppure no");
        JButton siButton = new JButton("SI");
        siButton.setBackground(Color.BLACK);
        siButton.setForeground(Color.WHITE);
        siButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                inputUtente = "si";

                frame.dispose();
            }
        });
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(JOptionPane.showConfirmDialog(frame,
                        "Sei sicuro di voler uscire? Se si, la sfida non avrà inizio!", "ATTENZIONE",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                        closed = true;
                        frame.dispose();
                }
            }
        });
        JButton noButton = new JButton("NO");
        noButton.setBackground(Color.BLACK);
        noButton.setForeground(Color.WHITE);
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                inputUtente = "no";

                frame.dispose();
            }
        });




        frame.add(this.welcomePanel);
        frame.add(stringSfida);

        frame.add(siButton);
        frame.add(noButton);

        this.frame.setLayout(new GridLayout(4,2));
        this.frame.pack();
        this.frame.validate();

        //il frame appare centrale allo schermo
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);


    }


    public String getResponse(){
        return this.inputUtente;
    }

    public boolean getClosed(){
        return this.closed;
    }

}