import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.TreeMap;

public class MainClient {
    /*
    CLASSE CHE IMPLEMENTA IL MAIN DI UN CLIENT
     */


    private static final int PORT_RMI = 6789;
    private static final int PORT_TCP = 1234;
    protected static final int PORT_UDP_SERVER = 3456;

    private static int PORT_UDP_CLIENT;
    protected static boolean checkSfida = false;

    public boolean getCheckSfida(){
        return this.checkSfida;
    }
    //FUNZIONE PER IL CALCOLO DELLA PORTA UDP DEL CLIENT
    private static int hashPort(String nome){
        int port = 0;
        for(int i = 0; i < nome.length(); i++){
            port = port + (int) nome.charAt(i);
        }
        return port + 1000;
    }

    public static void main(String[] args){
        ServerWQ serverObject;
        Remote RemoteObject;
        Socket socket;

        listenForSfida threadSfida = null;
        String username = null;
        char[] password = null;

        try{

            String line;
            boolean terminazione = true;
            socket = new Socket("localhost",PORT_TCP);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            GraphicInit init;
            int res_REGISTRA;
            long res_LOGIN = -1;
            //DO-WHILE PER REGISTRA_UTENTE E LOGIN (ESCO QUANDO HO EFFETTUATO CORRETTAMENTE IL LOGIN)
            do {
                init = new GraphicInit();
                while (init.getButtonClicked() == null) {
                    Thread.sleep(100);
                }
                String commandClicked = init.getButtonClicked();

                switch (commandClicked) {
                    case "REGISTRA_UTENTE":

                        try {
                            GraphicRegLogin regLogin = new GraphicRegLogin("REGISTRA_UTENTE");
                            //ATTENDO CHE L'UTENTE INVII UN COMANDO O PREMA SULLA X
                            while (!regLogin.getBackClicked() && ((username = regLogin.getUsername()) == null || (password = regLogin.getPassword()).equals(null))) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                                }
                            }

                            if(regLogin.getBackClicked()){
                                break;
                            }
                            if(password.length == 0){
                                JOptionPane.showMessageDialog(null, "ERRORE : PASSWORD NON INSERITA", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
                                break;
                            }
                            if(username.length() < 3 || username.length() > 128){
                                JOptionPane.showMessageDialog(null, "ERRORE : LUNGHEZZA USERNAME NON VALIDA", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
                                break;
                            }

                            // REGISTRAZIONE IN REMOTO
                            Registry r = LocateRegistry.getRegistry(PORT_RMI);
                            RemoteObject = r.lookup("QUIZZLE-SERVER");
                            serverObject = (ServerWQ) RemoteObject;




                            PORT_UDP_CLIENT = hashPort(username);
                            String passString = new String(password);
                            res_REGISTRA = serverObject.registra_utente(username, passString, PORT_UDP_CLIENT);

                            if (res_REGISTRA == -1)
                                JOptionPane.showMessageDialog(null, "UTENTE GIA' REGISTRATO", "ATTENZIONE", JOptionPane.INFORMATION_MESSAGE);

                            else {
                                JOptionPane.showMessageDialog(null, "UTENTE REGISTRATO", "ATTENZIONE", JOptionPane.INFORMATION_MESSAGE);
                            }

                            break;
                        } catch (RemoteException e) {
                            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                        } catch (NotBoundException e) {
                            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                        }
                    case "LOGIN":
                        long res = -1;

                            GraphicRegLogin regLogin = new GraphicRegLogin("LOGIN");
                            //ATTENDO CHE L'UTENTE INVII UN COMANDO O PREMA SULLA X
                            while (!regLogin.getBackClicked() && ((username = regLogin.getUsername()) == null || (password = regLogin.getPassword()).equals(null))) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                                }
                            }
                            if(regLogin.getBackClicked()){
                                break;

                            }
                            String passString = new String(password);
                            line = commandClicked.toLowerCase() + " " + username + " " + passString;
                            //SCRIVO IL COMANDO
                            writer.write(line);
                            writer.newLine();
                            writer.flush();
                            //ATTENDO IL JSONOBJECT DI RISPOSTA
                            BufferedReader readerjson = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String linejson = readerjson.readLine().concat("\n");

                            JSONParser parser = new JSONParser();
                            JSONObject objReceived = (JSONObject) parser.parse(linejson);

                            res_LOGIN = (long) objReceived.get("codice");
                            if (res_LOGIN == 0) {
                                JOptionPane.showMessageDialog(null, "LOGIN AVVENUTO CON SUCCESSO", "", JOptionPane.INFORMATION_MESSAGE);

                                PORT_UDP_CLIENT = hashPort(username);
                                //FACCIO PARTIRE IL THREAD LISTENER DELLA SFIDA
                                threadSfida = new listenForSfida(PORT_UDP_CLIENT, username);
                                threadSfida.start();
                                break;
                            } else {
                                JOptionPane.showMessageDialog(null, "ERRORE " + res + " " + objReceived.get("messaggioErrore"), "ATTENZIONE", JOptionPane.ERROR_MESSAGE);
                            }
                        break;

                }
            }while(res_LOGIN != 0);

            //DO WHILE PER L'ACCETTAZIONE DELLE RICHIESTE, FINCHE' NON ESEGUO IL LOGOUT
            do {
                GraphicInterfaceWQ wq = new GraphicInterfaceWQ(username);

                String[] Continuazione = new String[3];

                boolean back=false;


                while(wq.getClicked() == null){
                    Thread.sleep(100);
                }

                if(threadSfida.getSfidaArrivata()){
                    JOptionPane.showMessageDialog(null, "NON E' POSSIBILE ESEGUIRE ALTRI COMANDI\n MENTRE SI E' IMPEGNATI IN UNA SFIDA", "MOSTRA PUNTEGGIO", JOptionPane.INFORMATION_MESSAGE);
                    continue;
                }
                //ARRAY CONTENENTE LA PAROLE DEL COMANDO
                Continuazione[0] = wq.getClicked().toLowerCase();





                //SE HO FATTO LOGOUT METTO LA CONDIZIONE DEL WHILE A FALSE E UNA VOLTA PASSATO LO SWITCH ESCO
                if(Continuazione[0].compareTo("logout") == 0)
                    terminazione = false;
                GraphicInsertName insertName;
                //SWITCH CHE IN BASE AL COMANDO RICEVUTO COMPONE IL COMANDO COME RICHIESTO DA PROGETTO E LO INVIA AL CLIENTSERVICE
                switch(Continuazione[0]){
                    case "mostra_punteggio" :
                    case "mostra_classifica" :
                    case "lista_amici" :
                    case "logout" :
                        line = Continuazione[0].concat(" ").concat(username);

                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                        wq.invalidate();
                        wq.dispose();
                        line = null;
                        break;
                    case "aggiungi_amico" :
                        //APPARE IL FRAME CHE CHIEDE DI SCRIVERE IL NOME DELL'AMICO
                        insertName = new GraphicInsertName("aggiungi_amico");
                        while(insertName.getResponse() == null && !insertName.getBackClicked() ){
                            Thread.sleep(100);

                        }
                        if(insertName.getBackClicked()){
                            back=true;
                            break;
                        }
                        String nome = insertName.getResponse();
                        line = Continuazione[0].concat(" ").concat(username).concat(" ").concat(nome);
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                        line = null;
                        nome = null;
                        break;
                    case "sfida" :
                        //APPARE IL FRAME CHE CHIEDE IL NOME DELL'AMICO DA SFIDARE
                        insertName = new GraphicInsertName("sfida");
                        while(insertName.getResponse() == null && !insertName.getBackClicked()){
                            Thread.sleep(100);
                        }
                        if(insertName.getBackClicked()){
                            back=true;
                            break;
                        }
                        nome = insertName.getResponse();
                        line = Continuazione[0].concat(" ").concat(username).concat(" ").concat(nome);
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                        line = null;
                        break;

                }

                if(back){
                    continue;
                }
                //LEGGO LA RISPOSTA DA CLIENTSERVICE
                BufferedReader readerjson = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String linejson = readerjson.readLine().concat("\n");

                JSONParser parser = new JSONParser();
                JSONObject objReceived = (JSONObject) parser.parse(linejson);
                //PULIZIA BUFFER


                /*IN BASE AL COMANDO PRENDO IL RISULTATO (ED EVENTUALMENTE UN MESSAGGIO) CORRISPONDENTE
                 DAL FILE JSON RICEVUTO DAL CLIENTSERVICE. IN BASE AD ESSO FACCIO APPARIRE UN JOPTIONPANE
                 DI TIPO ERROR_MESSAGE O INFORMATION_MESSAGE
                 */
                switch(Continuazione[0]){
                    case "mostra_punteggio" :
                        if((long) objReceived.get("codice") == 0){
                            JOptionPane.showMessageDialog(null, "IL TUO PUNTEGGIO E' : " + objReceived.get("punteggio"), "MOSTRA PUNTEGGIO", JOptionPane.INFORMATION_MESSAGE);

                            break;
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "ERRORE " + objReceived.get("codice") + ": " + objReceived.get("messaggioErrore"), "ATTENZIONE", JOptionPane.ERROR_MESSAGE);

                            break;
                        }
                    case "mostra_classifica" :
                        if((long) objReceived.get("codice") == 0) {
                            // ORDINO LA CLASSIFICA PER MOSTRARLA CORRETTAMENTE ALL'UTENTE
                            HashMap<String, Long> map = new HashMap<String, Long>();
                            map = (HashMap) objReceived.get("classifica");

                            ValueComparator bvc = new ValueComparator(map);
                            TreeMap<String, Long> sorted_map = new TreeMap<String, Long>(bvc);
                            sorted_map.putAll(map);
                            JOptionPane.showMessageDialog(null, sorted_map, "CLASSIFICA", JOptionPane.INFORMATION_MESSAGE);

                            break;
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "ERRORE " + objReceived.get("codice") + ": " + objReceived.get("messaggioErrore"), "ATTENZIONE", JOptionPane.ERROR_MESSAGE);

                            break;
                        }
                    case "lista_amici" :
                        if((long) objReceived.get("codice") == 0){
                            JOptionPane.showMessageDialog(null, "LA TUA LISTA AMICI E' : " + objReceived.get("lista amici"), "LISTA AMICI", JOptionPane.INFORMATION_MESSAGE);


                            break;
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "ERRORE " + objReceived.get("codice") + ": " + objReceived.get("messaggioErrore"), "ATTENZIONE", JOptionPane.ERROR_MESSAGE);

                            break;
                        }
                    case "aggiungi_amico" :
                        if((long) objReceived.get("codice") == 0){
                            JOptionPane.showMessageDialog(null, "AMICO AGGIUNTO CON SUCCESSO!", "AGGIUNGI AMICO", JOptionPane.INFORMATION_MESSAGE);

                            break;
                        }
                        else{

                            JOptionPane.showMessageDialog(null, "ERRORE " + objReceived.get("codice") + ": " + objReceived.get("messaggioErrore"), "ATTENZIONE", JOptionPane.ERROR_MESSAGE);


                            break;
                        }
                    case "logout" :
                        if((long) objReceived.get("codice") == 0) {
                            JOptionPane.showMessageDialog(null, "LOGOUT ESEGUITO CON SUCCESSO!", "LOGOUT", JOptionPane.INFORMATION_MESSAGE);


                            break;
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "ERRORE " + objReceived.get("codice") + ": " + objReceived.get("messaggioErrore"), "ATTENZIONE", JOptionPane.ERROR_MESSAGE);

                            break;
                        }
                    case "sfida" :
                        if((long) objReceived.get("codice") == 0) {
                            //JOptionPane.showMessageDialog(null, "SFIDA INIZIATA CON SUCCESSO!", "SFIDA", JOptionPane.INFORMATION_MESSAGE);

                            break;
                        }
                        else if((long) objReceived.get("codice") == 5){
                            JOptionPane.showMessageDialog(null, "UTENTE GIA' IMPEGNATO IN UNA SFIDA, RIPROVARE IN SEGUITO", "SFIDA", JOptionPane.INFORMATION_MESSAGE);

                        }
                        else{
                            JOptionPane.showMessageDialog(null, "ERRORE " + objReceived.get("codice") + ": " + objReceived.get("messaggioErrore"), "ATTENZIONE", JOptionPane.ERROR_MESSAGE);

                            break;
                        }
                }



            }while(terminazione);







        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (SocketTimeoutException e){
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);


        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }

        System.exit(1);
    }
}