import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class listenForSfida extends Thread {
    private GraphicInterfaceSfida interfaceSfida;
    private GraphicInterfaceTraductions traductionsSfida;
    private static int PORT_UDP_THREAD;
    private static final int PORT_UDP_SERVER = 3456;
    private static int PORT_TCP_SFIDA = 7899;
    protected boolean sfidaTerminata = false;
    protected static boolean sfidaArrivata = false;
    private static String nameUtente;




    public listenForSfida(int port, String nome) {
        this.PORT_UDP_THREAD = port;
        nameUtente = nome;


    }
    public static boolean isParsable(String input){
        try {
            Integer.parseInt(input);
            return true;

        }
        catch (NumberFormatException e){
            return false;
        }
    }


    //FUNZIONE DI AGGIORNAMENTO DEL PUNTEGGIO CONTENUTO NEL FILE DELLO SFIDANTE/SFIDATO
    public void updateFile(String parola) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        String pathUser = "./Users";
        String path = pathUser.concat("/").concat(nameUtente).concat(".json");



        Object obj = parser.parse(new FileReader(path));
        JSONObject objnew = (JSONObject) obj;
        long punti = (long) objnew.get("punteggio");
        int puntiInt = Math.toIntExact(punti);
        puntiInt += Integer.parseInt(parola);
        //IL PUNTEGGIO MINIMO E' 0, NON ESISTONO PUNTEGGI NEGATIVI NEI FILE JSON DEGLI UTENTI
        if(puntiInt < 0)
            puntiInt = 0;
        JSONObject jsonObj = new JSONObject();
        ArrayList<String> listaAmici = (ArrayList<String>) objnew.get("lista amici");
        String pass = (String) objnew.get("password");
        String user = (String) objnew.get("nome");
        jsonObj.put("nome", user);
        jsonObj.put("password", pass);
        jsonObj.put("punteggio", puntiInt);
        jsonObj.put("lista amici", listaAmici);


        try (FileWriter filejson = new FileWriter(path)) {
            filejson.write(jsonObj.toJSONString());
            filejson.flush();
            filejson.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getSfidaArrivata(){
        return sfidaArrivata;
    }

    public boolean getSfidaTerminata(){
        return sfidaTerminata;
    }



    @Override
    public void run() {


        while (!listenForSfida.interrupted()) {
            sfidaArrivata = false;
            DatagramSocket serverSocket = null;
            String nomeSfidante = null;


            try {
                //DICHIARAZIONE DATAGRAM SOCKET DI ASCOLTO (CON RELATIVO DATAGRAM PACKET)
                serverSocket = new DatagramSocket(PORT_UDP_THREAD);
                serverSocket.setReuseAddress(true);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);



                nomeSfidante = new String(receivePacket.getData(), 0, receivePacket.getLength());


                String risposta = null;
                //LATO SFIDANTE
                if (nomeSfidante.compareTo("si") == 0) {
                    sfidaArrivata = true;
                    serverSocket.close();
                    ServerSocket socket = new ServerSocket(2222);
                    Socket writable = socket.accept();

                    SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 7899));


                    ByteBuffer buffer = ByteBuffer.allocate(256);


                    //RIPROVO IL COLLEGAMENTO AL SELECTOR FINCHE' NON HA SUCCESSO
                    while (!client.isConnected()) {
                        client = SocketChannel.open(new InetSocketAddress("localhost", 7899));
                    }


                    try {

                        while (true) {
                            Timer t = new Timer();
                            //SE SCATTA IL TIMEOUT FORZO IL CLICK SU INVIA PER FAR ARRIVARE IL PUNTEGGIO
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    traductionsSfida.inviaButton.doClick();

                                }
                            };

                            t.schedule(timerTask, 60000);


                            BufferedReader reader = new BufferedReader(new InputStreamReader(writable.getInputStream()));
                            String parola = reader.readLine();

                            if (isParsable(parola)) {
                                //APPARE FRAME CON IL PUNTEGGIO OTTENUTO
                                JOptionPane.showMessageDialog(null, "PUNTEGGIO OTTENUTO : " + Integer.parseInt(parola), "INTERFACCIA DI " + nameUtente, JOptionPane.INFORMATION_MESSAGE);
                                updateFile(parola);
                                socket.close();
                                client.close();
                                t.cancel();
                                break;
                            }
                            if (parola.contains("TIMEOUT")) {
                                //SE E' SCADUTO IL TIMEOUT
                                this.traductionsSfida.dispose();
                                String[] finishTime = parola.split("/");
                                JOptionPane.showMessageDialog(null, "TEMPO SCADUTO! PUNTEGGIO OTTENUTO : " + finishTime[1], "INTERFACCIA DI " + nameUtente, JOptionPane.INFORMATION_MESSAGE);
                                updateFile(finishTime[1]);
                                socket.close();
                                client.close();
                                break;
                            }

                            this.traductionsSfida = new GraphicInterfaceTraductions(nameUtente, parola);
                            //FINCHE' NON PREMO X O NON INVIO NULLA ATTENDO
                            while (this.traductionsSfida.getResponse() == null && !this.traductionsSfida.getClosed()) {
                                Thread.sleep(100);
                            }

                            if(this.traductionsSfida.getClosed()){
                                //SE HO PREMUTO LA X
                                JOptionPane.showMessageDialog(null, "PUNTEGGIO OTTENUTO : 0", "INTERFACCIA DI " + nameUtente, JOptionPane.INFORMATION_MESSAGE);
                                updateFile("0");
                                risposta = "W1";
                                ByteBuffer bufferRes = ByteBuffer.allocate(256);
                                bufferRes = ByteBuffer.wrap(risposta.getBytes());
                                client.write(bufferRes);
                                buffer.clear();
                                bufferRes.clear();
                                socket.close();
                                client.close();
                                t.cancel();
                                risposta = null;
                                break;
                            }
                            //UNO E' LO SFIDANTE
                            risposta = this.traductionsSfida.getResponse() + "/uno/";
                            this.traductionsSfida.invalidate();

                            ByteBuffer bufferRes = ByteBuffer.allocate(256);
                            bufferRes = ByteBuffer.wrap(risposta.getBytes());
                            client.write(bufferRes);
                            buffer.clear();
                            bufferRes.clear();
                            risposta = null;


                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (nomeSfidante.compareTo("no") == 0) {
                    JOptionPane.showMessageDialog(null, "SFIDA NON ACCETTATA", "INTERFACCIA DI " + nameUtente, JOptionPane.INFORMATION_MESSAGE);
                    sfidaArrivata = false;
                    nomeSfidante = null;
                    serverSocket.close();
                    continue;
                }

                //LATO SFIDATO
                if (nomeSfidante.compareTo("si") != 0) {
                    sfidaArrivata = true;
                    this.interfaceSfida = new GraphicInterfaceSfida(nameUtente, nomeSfidante);
                    //FINCHE' NON PREMO X O NON INVIO NULLA ATTENDO
                    while (this.interfaceSfida.getResponse() == null && !this.interfaceSfida.getClosed()) {
                        Thread.sleep(100);
                    }
                    if(this.interfaceSfida.getClosed() || this.interfaceSfida.getResponse().compareTo("si") != 0){
                        //SE HO PREMUTO LA X OPPURE PREMO NO SIGNIFICA CHE LA SFIDA E' STATA RIFIUTATA
                        String line = "no";
                        InetAddress IP = InetAddress.getByName("localhost");
                        byte[] sendData;
                        sendData = line.getBytes();
                        DatagramPacket sendRes = new DatagramPacket(sendData, sendData.length, IP, PORT_UDP_SERVER);
                        sfidaArrivata = false;
                        serverSocket.send(sendRes);
                        nomeSfidante = null;
                        serverSocket.close();
                        break;
                    }
                    else {
                        //ALTRIMENTI HO PREMUTO SI E QUINDI HO ACCETTATO LA SFIDA

                        String line = "si";
                        InetAddress IP = InetAddress.getByName("localhost");
                        byte[] sendData;
                        sendData = line.getBytes();
                        DatagramPacket sendRes = new DatagramPacket(sendData, sendData.length, IP, PORT_UDP_SERVER);
                        serverSocket.send(sendRes);
                        serverSocket.close();
                        ServerSocket socket = new ServerSocket(1111);

                        Socket writable = socket.accept();

                        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 7899));



                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        //RIPROVO IL COLLEGAMENTO AL SELECTOR FINCHE' NON HA SUCCESSO
                        while (!client.isConnected()) {
                            client = SocketChannel.open(new InetSocketAddress("localhost", 7899));
                        }


                        try {



                            while (true) {
                                Timer t = new Timer();
                                //SE E' SCADUTO IL TIMER FORZO L'INVIO PER FAR TERMINARE LA SFIDA
                                TimerTask timerTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        traductionsSfida.inviaButton.doClick();
                                    }
                                };

                                t.schedule(timerTask, 60000);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(writable.getInputStream()));
                                String parola = reader.readLine();
                                //INSERIRE CONTROLLO SE L'UTENTE CRETINO SCRIVE NUMERI A CASO
                                if (isParsable(parola)) {
                                    //APPARE FRAME CON IL PUNTEGGIO OTTENUTO
                                    JOptionPane.showMessageDialog(null, "PUNTEGGIO OTTENUTO : " + Integer.parseInt(parola), "INTERFACCIA DI " + nameUtente, JOptionPane.INFORMATION_MESSAGE);
                                    updateFile(parola);
                                    sfidaTerminata = true;
                                    socket.close();
                                    client.close();
                                    t.cancel();
                                    break;
                                }

                                if (parola.contains("TIMEOUT")) {
                                    //SE E' SCADUTO IL TIMEOUT
                                    String[] finishTime = parola.split("/");
                                    JOptionPane.showMessageDialog(null, "TEMPO SCADUTO! PUNTEGGIO OTTENUTO : " + finishTime[1], "INTERFACCIA DI " + nameUtente, JOptionPane.INFORMATION_MESSAGE);
                                    updateFile(finishTime[1]);
                                    sfidaTerminata = true;
                                    socket.close();
                                    client.close();
                                    break;
                                }
                                this.traductionsSfida = new GraphicInterfaceTraductions(nameUtente, parola);


                                while (this.traductionsSfida.getResponse() == null && !this.traductionsSfida.getClosed()) {
                                    Thread.sleep(100);
                                }
                                //DUE E' LO SFIDATO
                                if(this.traductionsSfida.getClosed()){

                                    JOptionPane.showMessageDialog(null, "PUNTEGGIO OTTENUTO : 0", "INTERFACCIA DI " + nameUtente, JOptionPane.INFORMATION_MESSAGE);
                                    updateFile("0");
                                    risposta = "W2";
                                    ByteBuffer bufferRes = ByteBuffer.allocate(256);
                                    bufferRes = ByteBuffer.wrap(risposta.getBytes());
                                    client.write(bufferRes);
                                    buffer.clear();
                                    bufferRes.clear();
                                    socket.close();
                                    client.close();
                                    t.cancel();
                                    risposta = null;
                                    break;
                                }
                                risposta = this.traductionsSfida.getResponse() + "/due/";

                                ByteBuffer bufferRes = ByteBuffer.allocate(256);
                                bufferRes = ByteBuffer.wrap(risposta.getBytes());
                                client.write(bufferRes);
                                buffer.clear();
                                bufferRes.clear();
                                risposta = null;



                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                }


            } catch (ConnectException e) {
                JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "INTERFACCIA DI " + nameUtente, JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (SocketException e) {
                JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "INTERFACCIA DI " + nameUtente, JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();


            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "INTERFACCIA DI " + nameUtente, JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();

            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "INTERFACCIA DI " + nameUtente, JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }



        }
    }

}