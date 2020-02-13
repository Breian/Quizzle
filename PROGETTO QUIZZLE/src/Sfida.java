import javax.swing.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.*;

public class Sfida implements Runnable {
    //CLASSE CHE IMPLEMENTA IL THREAD SFIDA
    protected boolean sfidaTerminata = false;
    private String nickUtente;
    private int punteggioUtente;
    private int punteggioAmico;
    private ArrayList<String> translatedWords;
    private ArrayList<String> arrWords;
    private static int PORT_TCP_AMICO;
    private static int PORT_TCP_UTENTE;

    private boolean sendPunteggioUtente = false;
    private boolean sendPunteggioAmico = false;


    protected Sfida(String nU, ArrayList<String> translatedWords, ArrayList<String> arrWords, int port, int portSfida) {
        this.nickUtente = nU;
        this.translatedWords = translatedWords;
        this.arrWords = arrWords;
        this.punteggioUtente = 0;
        this.punteggioAmico = 0;
        PORT_TCP_AMICO = port;
        PORT_TCP_UTENTE = portSfida;

    }

    protected int getPunteggioUtente() {
        return this.punteggioUtente;
    }

    protected boolean getSfidaTerminata(){
        return this.sfidaTerminata;
    }

    @Override
    public void run() {
        /*PER OGNI RISPOSTA ESATTA +3 PUNTI
          PER OGNI RISPOSTA SBAGLIATA -2 PUNTI
         */

        try{

            BufferedWriter writer1;
            BufferedWriter writer2;

            //DICHIARO ED APRO IL SELECTOR USANDO NIO
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress("localhost", 7899));
            serverSocket.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(256);
            int i = 0;
            int j = 0;


            Socket socketW2 = new Socket("localhost", 1111); //SFIDANTE
            Socket socketW1 = new Socket("localhost", 2222); //SFIDATO
            writer1 = new BufferedWriter(new OutputStreamWriter(socketW1.getOutputStream()));
            writer1.write(this.arrWords.get(i));
            writer1.newLine();
            writer1.flush();
            i++;

            writer2 = new BufferedWriter(new OutputStreamWriter(socketW2.getOutputStream()));
            writer2.write(this.arrWords.get(j));
            writer2.newLine();
            writer2.flush();
            j++;

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    //SE SCADE IL TIMER INVIO IL PUNTEGGIO
                    try {
                        BufferedWriter writer1 = new BufferedWriter(new OutputStreamWriter(socketW1.getOutputStream()));
                        writer1.write("TIMEOUT".concat("/").concat(String.valueOf(punteggioUtente)));
                        writer1.newLine();
                        writer1.flush();

                        BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(socketW2.getOutputStream()));
                        writer2.write("TIMEOUT".concat("/").concat(String.valueOf(punteggioAmico)));
                        writer2.newLine();
                        writer2.flush();

                        socketW1.close();
                        socketW2.close();
                        serverSocket.close();
                        selector.close();
                        Thread.currentThread().interrupt();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    }


                }
            };
            Timer t = new Timer();

            t.schedule(timerTask,60000);

            //CICLO DEL SELECTOR
            while(selector.select() > 0) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    String received = null;
                    SocketChannel client = null;

                    SelectionKey key = iter.next();
                    iter.remove();


                    try {
                        if (key.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            client = server.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);

                        }

                        if (key.isReadable()) {
                            client = (SocketChannel) key.channel();


                            client.read(buffer);

                            received = new String(buffer.array()).trim().toLowerCase();

                            //PULISCO OGNI VOLTA IL BUFFER
                            Arrays.fill(buffer.array(), (byte) 0);
                            //RITORNO ALLA MODALITA' SCRITTURA
                            buffer.clear();


                        }
                        if (received != null) {

                            String[] risposta = received.split("/");

                            if(risposta.length < 2){
                                key.cancel();
                                sfidaTerminata = true;
                                if(risposta[0].compareTo("W1") == 0) {
                                    socketW1.close();
                                    this.sendPunteggioUtente = true;

                                }
                                else if(risposta[0].compareTo("W2") == 0){
                                    socketW2.close();
                                    this.sendPunteggioAmico = true;
                                }
                                break;
                            }
                            if (risposta[1].compareTo("uno") == 0) {
                                //AGGIORNAMENTO PUNTEGGIO
                                if (risposta[0].compareTo(this.translatedWords.get(i - 1)) == 0) {
                                    this.punteggioUtente += 3;
                                }
                                else {
                                    this.punteggioUtente -= 2;
                                }
                                //SE SONO ARRIVATO IN FONDO ALLE PAROLE INVIO IL RISULTATO
                                if (i == this.arrWords.size()) {
                                    writer1 = new BufferedWriter(new OutputStreamWriter(socketW1.getOutputStream()));
                                    writer1.write(Integer.toString(punteggioUtente));
                                    writer1.newLine();
                                    writer1.flush();
                                    this.sendPunteggioUtente = true;
                                    key.cancel();
                                    socketW1.close();

                                }
                                else {
                                    writer1 = new BufferedWriter(new OutputStreamWriter(socketW1.getOutputStream()));
                                    writer1.write(this.arrWords.get(i));
                                    writer1.newLine();
                                    writer1.flush();
                                    i++;

                                    Arrays.fill(risposta, null);
                                }
                            }
                            else if (risposta[1].compareTo("due") == 0) {


                                if (risposta[0].compareTo(this.translatedWords.get(j - 1)) == 0) {
                                    this.punteggioAmico += 3;
                                }
                                else {
                                    this.punteggioAmico -= 2;
                                }
                                if (j == this.arrWords.size()) {
                                    writer2 = new BufferedWriter(new OutputStreamWriter(socketW2.getOutputStream()));
                                    writer2.write(Integer.toString(punteggioUtente));
                                    writer2.newLine();
                                    writer2.flush();
                                    this.sendPunteggioAmico = true;
                                    key.cancel();
                                    socketW2.close();

                                }
                                else {
                                    writer2 = new BufferedWriter(new OutputStreamWriter(socketW2.getOutputStream()));
                                    writer2.write(this.arrWords.get(j));
                                    writer2.newLine();
                                    writer2.flush();
                                    j++;

                                    Arrays.fill(risposta, null);
                                }

                            }

                        }


                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    }


                }
                if (sendPunteggioUtente && sendPunteggioAmico) {
                    serverSocket.close();
                    selector.close();
                    sfidaTerminata = true;
                    break;
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        this.arrWords.clear();
        this.translatedWords.clear();

    }


}