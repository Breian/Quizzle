import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GraphicInterfaceWQ extends JFrame {
    private JFrame frame;

    private JLabel WordPanel;
    protected String response = null;
    protected String clicked = null;
    GraphicInterfaceWQ(String utente){
        //CLASSE DEL FRAME PRINCIPALE CONTENENTE TUTTI I COMANDI TRANNE "LOGIN" E "REGISTRA_UTENTE"
        this.frame = new JFrame("INTERFACCIA DI " + utente.toUpperCase());
        this.frame.setPreferredSize(new Dimension(400,180));
        this.frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.frame.setResizable(false);
        JButton aggiungiAmicoButton = new JButton("AGGIUNGI_AMICO");
        aggiungiAmicoButton.setBackground(Color.BLACK);
        aggiungiAmicoButton.setForeground(Color.WHITE);
        aggiungiAmicoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicked = aggiungiAmicoButton.getActionCommand();
                frame.dispose();
            }
        });


        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setBackground(Color.BLACK);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicked = logoutButton.getActionCommand();
                frame.dispose();

            }
        });
        JButton listaAmiciButton = new JButton("LISTA_AMICI");
        listaAmiciButton.setBackground(Color.BLACK);
        listaAmiciButton.setForeground(Color.WHITE);
        listaAmiciButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicked = listaAmiciButton.getActionCommand();
                frame.dispose();
            }
        });
        JButton sfidaButton = new JButton("SFIDA");
        sfidaButton.setBackground(Color.BLACK);
        sfidaButton.setForeground(Color.WHITE);
        sfidaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicked = sfidaButton.getActionCommand();
                frame.dispose();
            }
        });
        JButton mostraClassificaButton = new JButton("MOSTRA_CLASSIFICA");
        mostraClassificaButton.setBackground(Color.BLACK);
        mostraClassificaButton.setForeground(Color.WHITE);
        mostraClassificaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicked = mostraClassificaButton.getActionCommand();
                frame.dispose();
            }
        });
        JButton mostraPunteggioButton = new JButton("MOSTRA_PUNTEGGIO");
        mostraPunteggioButton.setBackground(Color.BLACK);
        mostraPunteggioButton.setForeground(Color.WHITE);
        mostraPunteggioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicked = mostraPunteggioButton.getActionCommand();
                frame.dispose();
            }
        });

        this.WordPanel = new JLabel("<html><FONT COLOR=BLUE>" + "WORD QUIZZLE " + "</FONT></html>", SwingConstants.RIGHT);
        // MOUSE MOTION LISTENER PER FAR APPARIRE UNA BREVE DESCRIZIONE DEL COMANDO
        this.frame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {}

            @Override
            public void mouseMoved(MouseEvent e) {

                aggiungiAmicoButton.setToolTipText("AGGIUNGI UN AMICO INSERENDO IL SUO NOME!");
                sfidaButton.setToolTipText("SFIDA UN TUO AMICO IN UNA GARA DI TRADUZIONI INGLESE-ITALIANO!");
                listaAmiciButton.setToolTipText("CLICCA PER VEDERE LA TUA LISTA AMICI!");
                logoutButton.setToolTipText("CLICCA PER EFFETTUARE IL LOGOUT!");
                mostraClassificaButton.setToolTipText("CLICCA E GUARDA LA TUA POSIZIONE IN CLASSIFICA (COMPOSTA DA TE E DAI TUOI AMICI)");
                mostraPunteggioButton.setToolTipText("CLICCA PER FAR APPARIRE IL TUO PUNTEGGIO!");
            }
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
        this.frame.add(this.WordPanel);
        this.frame.add(new JLabel("<html><FONT COLOR=BLUE>" + "  2019/2020" + "</FONT></html>", SwingConstants.LEFT));
        this.frame.add(aggiungiAmicoButton);
        this.frame.add(listaAmiciButton);
        this.frame.add(logoutButton);
        this.frame.add(sfidaButton);
        this.frame.add(mostraClassificaButton);
        this.frame.add(mostraPunteggioButton);

        this.frame.setLayout(new GridLayout(4,2));
        this.frame.pack();
        this.frame.validate();

        //il frame appare centrale allo schermo
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);


    }

    public String getClicked(){
        return this.clicked;
    }
    protected String getResponse(){
        return this.response;
    }
}