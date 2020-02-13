import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;

public class ClientService extends Thread {
    //CLASSE CHE GESTISCE LE RICHIESTE DEL CLIENT
    private boolean Connected = true;
    private Socket socket;
    private ServerQuizzle SQ;
    //COSTRUTTORE
    protected ClientService(Socket s, ServerQuizzle q) {
        this.SQ = q;
        this.socket = s;
    }

    public void run() {
        String[] elements = null;
        try {
            while (this.Connected) {
                //LEGGO LA RICHIESTA DEL CLIENT
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                String line = reader.readLine();
                elements = line.split(" ");
                //RESULT CONTIENE IL JSON OBJECT RISULTATO DEL COMANDO
                JSONObject result = parseExecution(line);


                    //SCRIVO IL JSON OBJECT RISULTATO
                BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
                writer2.write(result.toString() + "\n");
                writer2.newLine();
                writer2.flush();



            }
        } catch(SocketException e){
            Connected = false;
            SQ.removeUser(elements[1]);
            //SE IL CLIENT CHIUDE IMPROVVISAMENTE RIMUOVO L'UTENTE DALL' ARRAYLIST UTENTILOGGATI
            JOptionPane.showMessageDialog(null, "ERRORE : CONNESSIONE CHIUSA IMPROVVISAMENTE", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
            SQ.removeUser(elements[1]);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }
    }

    private JSONObject parseExecution(String line) {
        // FUNZIONE CHE IN BASE AL COMANDO COSTRUISCE IL JSONOBJECT RISULTATO DELL'OPERAZIONE
        if (line != null) {

            JSONObject result = new JSONObject();
            String[] elements = line.split(" ");

            /*
            IL JSON OBJECT RISULTATO CONTIENE UN INTERO CHE VALE 0 SE IL COMANDO HA AVUTO SUCCESSO,
            ALTRIMENTI UN ALTRO VALORE NUMERICO CHE VARIA IN BASE ALL'ERRORE.
            IN QUEST'ULTIMO CASO VIENE INSERITO ANCHE UN MESSAGGIO CONTENENTE IL SIGNIFICATO DEL CODICE DI ERRORE.
            */
            switch (elements[0]) {
                case "registra_utente" :
                    break;
                case "login":
                    try {

                        if(elements.length <= 2){
                            result.put("messaggioErrore", "nickUtente o password nulli");
                            result.put("codice", 4);
                            return result;
                        }
                        int res = SQ.login(elements[1], elements[2]);

                        switch (res) {
                                case 0 :
                                    result.put("codice", res);
                                    return result;
                                case 1:
                                    result.put("messaggioErrore", "utente già loggato");
                                    result.put("codice", res);
                                    return result;
                                case 2:
                                    result.put("messaggioErrore", "password sbagliata");
                                    result.put("codice", res);
                                    return result;
                                case 3:
                                    result.put("messaggioErrore", "utente non registrato");
                                    result.put("codice", res);
                                    return result;
                                case 4:
                                    result.put("messaggioErrore", "nickUtente o password nulli");
                                    result.put("codice", res);
                                    return result;
                            }


                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
                    }
                case "logout":
                    try {
                        int res = SQ.logout(elements[1]);
                        if (res != 0) {
                            switch (res) {
                                case 1:
                                    result.put("messaggioErrore", "utente non loggato");
                                    result.put("codice", res);
                                    return result;
                                case 2:
                                    result.put("messaggioErrore", "utente nullo");
                                    result.put("codice", res);
                                    return result;
                            }
                        }

                        result.put("codice", res);
                        this.Connected = false;
                        return result;

                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    }
                case "aggiungi_amico":
                    try {
                        int res = SQ.aggiungi_amico(elements[1], elements[2]);
                        if (res != 0) {
                            switch (res) {
                                case 1:
                                    result.put("messaggioErrore", "utente non loggato");
                                    result.put("codice", res);
                                    return result;
                                case 2:
                                    result.put("messaggioErrore", "amico inesistente");
                                    result.put("codice", res);
                                    return result;
                                case 3:
                                    result.put("messaggioErrore", "amico già presente in lista amici");
                                    result.put("codice", res);
                                    return result;
                                case 4:
                                    result.put("messaggioErrore", "nickUtente o nickAmico nulli");
                                    result.put("codice", res);
                                    return result;
                                case 5 :
                                    result.put("messaggioErrore", "non si può aggiungere se stessi");
                                    result.put("codice", res);
                                    return result;
                            }
                        }

                        result.put("codice", res);
                        return result;

                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    }
                case "lista_amici":
                    try {
                        result = SQ.lista_amici(elements[1]);
                        return result;
                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    }
                case "mostra_punteggio":
                    try {

                        int res = SQ.mostra_punteggio(elements[1]);
                        if (res < 0) {
                            switch (res) {
                                case -1:
                                    result.put("messaggioErrore", "utente non loggato");
                                    result.put("codice", res);
                                    return result;
                                case -2:
                                    result.put("messaggioErrore", "utente nullo");
                                    result.put("codice", res);
                                    return result;

                            }
                        }

                        result.put("punteggio", res);
                        result.put("codice", 0);
                        return result;

                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    }
                case "mostra_classifica":
                    try {
                        result = SQ.mostra_classifica(elements[1]);
                        return result;
                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    }
                case "sfida" :
                    try {

                        int res = SQ.sfida(elements[1], elements[2]);

                        switch(res){
                            case 0 :
                                result.put("codice", res);
                                return result;
                            case 1 :
                                result.put("messaggioErrore", "utente non loggato");
                                result.put("codice", res);
                                return result;
                            case 2 :
                                result.put("messaggioErrore", "amico non loggato");
                                result.put("codice", res);
                                return result;
                            case 3 :
                                result.put("messaggioErrore", "timeout di accettazione sfida scaduto");
                                result.put("codice", res);
                                return result;
                            case 4 :
                                result.put("messaggioErrore", "amico o utente nulli");
                                result.put("codice", res);
                                return result;
                            case 5 :
                                result.put("codice", res);
                                return result;
                            case 6 :
                                result.put("messaggioErrore", "non siete amici");
                                result.put("codice", res);
                                return result;
                            case -1 :
                                result.put("messaggioErrore", "non si può sfidare se stessi");
                                result.put("codice", res);
                                return result;
                        }
                        break;
                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    }
                default :
                    JOptionPane.showMessageDialog(null, "ERRORE : COMANDO NON RICONOSCIUTO", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                    return result;

            }
            return result;
        }
        return null;
    }
}