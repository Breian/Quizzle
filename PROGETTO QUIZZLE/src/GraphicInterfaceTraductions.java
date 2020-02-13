import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GraphicInterfaceTraductions extends JFrame implements ActionListener {
    private JFrame frame;
    private JTextField textField;
    private JLabel WordPanel;
    protected String response;
    protected boolean closed = false;
    JButton inviaButton;
    public GraphicInterfaceTraductions(String nomeUtente, String word){
        //CLASSE DEL FRAME DI TRADUZIONE DELLE PAROLE
        this.frame = new JFrame("Thread di " + nomeUtente);
        this.frame.setPreferredSize(new Dimension(400,180));
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.setResizable(false);
        this.WordPanel = new JLabel("Parola da tradurre : " + word);
        this.inviaButton = new JButton("INVIA");
        this.inviaButton.setBackground(Color.BLACK);
        this.inviaButton.setForeground(Color.WHITE);
        inviaButton.addActionListener(this);
        this.textField = new JTextField();
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(JOptionPane.showConfirmDialog(frame,
                        "Sei sicuro di voler uscire? Non arriveranno altre parole da tradurre\n e la tua sfida terminerà con punteggio 0!", "ATTENZIONE",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                        closed = true;
                        frame.dispose();
                }
            }
        });
        this.frame.add(WordPanel);
        this.frame.add(textField);
        this.frame.add(inviaButton);
        this.frame.setLayout(new GridLayout(3,2));
        this.frame.pack();
        this.frame.validate();


        //il frame appare centrale allo schermo
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.response = textField.getText();

        frame.dispose();
    }

    public String getResponse(){
        return this.response;
    }

    public boolean getClosed(){
        return this.closed;
    }
}