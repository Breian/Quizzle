import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class ServerQuizzle extends RemoteServer implements ServerWQ {
    /*
    CLASSE CHE IMPLEMENTA L'INTERFACCIA SERVERWQ. CONTIENE TUTTE LE FUNZIONI RICHIESTE DA PROGETTO.
     */

    private final ReentrantLock lockUtentiLoggati = new ReentrantLock();
    private final ReentrantLock lockSfidaInCorso = new ReentrantLock();
    private final ReentrantLock lockPortMap = new ReentrantLock();
    private final ReentrantLock lockPrepareArrays = new ReentrantLock();

    private static final int PORT_UDP = 3456;
    private String pathUser = "./Users";

    private ArrayList<String> utentiLoggati;
    private ThreadPoolExecutor executor; //THREAD POOL DELLE SFIDE
    private ArrayList<String> sfidaInCorso;

    private HashMap<String,Integer> portMap;
    private ArrayList<String> translatedWords;
    private ArrayList<String> arrWords;

    private File filePorts;
    protected ServerQuizzle() throws RemoteException {
        boolean folder = new File(pathUser).mkdir();
        utentiLoggati = new ArrayList<>();
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.sfidaInCorso = new ArrayList<>();
        this.portMap = new HashMap<>();
        this.translatedWords = new ArrayList<>();
        this.arrWords = new ArrayList<>();
        this.filePorts = new File(pathUser.concat("/").concat("PortMap").concat(".txt"));


    }
    // FUNZIONE CHE RIMUOVE UN UTENTE LOGGATO SE ESSO HA CHIUSO IMPROVVISAMENTE IL CLIENT
    protected void removeUser(String utente){
        boolean acquireLock;
        while(!(acquireLock = lockUtentiLoggati.tryLock())){
            try {
                wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.utentiLoggati.remove(utente);
        lockUtentiLoggati.unlock();
    }
    //FUNZIONE CHE CONTROLLA SE L'UTENTE CHE VOGLIO SFIDARE E' NELLA MIA LISTA AMICI
    private boolean controllaAmico(String nickUtente, String nickAmico) {
        if(nickUtente.length() == 0 || nickAmico.length() == 0)
            return false;
        try {
            //AGGIUNGO NICKAMICO ALLA LISTA AMICI DI NICKUTENTE
            JSONParser parser = new JSONParser();
            String path = pathUser.concat("/").concat(nickUtente).concat(".json");
            JSONObject jsonObj = new JSONObject();


            Object obj = parser.parse(new FileReader(path));
            JSONObject oldfile = (JSONObject) obj;
            ArrayList<String> listaAmici = (ArrayList<String>) oldfile.get("lista amici");
            if (listaAmici.contains(nickAmico)) {
                //SIETE GIA' AMICI
                return true;
            }
            else return false;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    // FUNZIONE CHE CONTROLLA SE HO SCELTO UNA PORTA NON DI QUELLE DI BASE USATE DAL PROGRAMMA
    private boolean controlKnownPorts(int port){
        if(port == 7899 || port == 1234 || port == 6789 || port == 3456)
            return true;
        return false;
    }

    // FUNZIONE CHE COSTRUISCE SOTTO FORMA DI STRINGA LA RISPOSTA PROVENIENTE DAL SITO DI TRADUZIONE
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int count;
        while((count = rd.read()) != -1){
            sb.append((char) count);
        }
        return sb.toString();
    }

    //FUNZIONE CHE MI TRADUCE LA PAROLA INVIANDOLA AL SERVER FORNITO DA PROGETTO
    private String translate(String text){


        try {
            String query = URLEncoder.encode(text, "UTF-8");
            String langpair = URLEncoder.encode("it"+"|"+"en", "UTF-8");
            String url = "http://mymemory.translated.net/api/get?q="+query+"&langpair="+langpair;
            InputStream is = new URL(url).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsontext = readAll(reader);

            JSONParser parser = new JSONParser();

            Object o = parser.parse(jsontext);
            JSONObject obj = (JSONObject) o;
            JSONObject translate = (JSONObject) obj.get("responseData");
            return (String) translate.get("translatedText");



        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }
        return null;
    }

    // FUNZIONE CHE MI TROVA UNA PAROLA DAL DIZIONARIO, ASSICURANDOSI CHE NON NE VENGANO PRESE DUE UGUALI
    private String foundWord(File f, int k) throws FileNotFoundException {
        if(k < 0){
            return null;
        }
        BufferedReader reader = new BufferedReader(new FileReader(f));


        try {
            String line = null;
            for(int i = 0; i < k; i++) {
                line = reader.readLine();
            }
            return line;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }

        return null;

    }
    // FUNZIONE CHE MI PREPARA GLI ARRAYLIST DELLE PAROLE DA TRADURRE E LA LORO TRADUZIONE
    private void PrepareArrays() {
        boolean acquireLock;
        while(!(acquireLock = lockPrepareArrays.tryLock())){
            try {
                wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        try {
            String data = "Dizionario.txt";
            File out = new File("./".concat(data));
            //SELEZIONO AL MASSIMO 6 PAROLE
            int numParole = 4;


            for (int i = 0; i < numParole; i++) {
                //PRENDO LA PAROLA DI POSIZIONE K DAL DIZIONARIO E LA BUTTO DENTRO UN ARRAY DI PAROLE

                int K = (int) (Math.random() * 9) + 1;



                String word = foundWord(out, K);
                if (this.arrWords.indexOf(word) == -1) {
                    this.arrWords.add(word);
                } else {
                    i--;

                }
            }


            for (int i = 0; i < numParole; i++) {
                //PER OGNI PAROLA MI SALVO LA TRADUZIONE IN UN ALTRO ARRAY (NELLA STESSA POSIZIONE)
                String res = translate(arrWords.get(i));

                this.translatedWords.add(res);
            }
            lockPrepareArrays.unlock();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }
    }

    @Override
    //REGISTRA_UTENTE
    public int registra_utente(String nickUtente, String password, int port_UDP) throws RemoteException {
        /*
        return 0 = tutto ok
        return -1 = file già esistente (e quindi utente già registrato)
         */
        String current = nickUtente.concat(".json");
        ArrayList<String> lista = new ArrayList<>();

        //CONTROLLO SE IL FILE ESISTE NELLA LISTA
        File folder = new File("./Users");
        File[] ListOfFiles = folder.listFiles();
        for (int i = 0; i < ListOfFiles.length; i++) {
            if (ListOfFiles[i].getName().compareTo(current) == 0) {
                return -1;

            }
        }

        String path = pathUser.concat("/").concat(nickUtente).concat(".json");

        JSONObject user = new JSONObject();
        user.put("nome", nickUtente);
        user.put("password", password);
        user.put("lista amici", lista);
        user.put("punteggio", 0);
        //SALVO LA PORTA IN UNA HASHMAP CHE ANDRO' A SALVARE IN UN FILE
        boolean acquireLock;
        while(!(acquireLock = lockPortMap.tryLock())){
            try {
                wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.portMap.put(nickUtente, port_UDP);
        lockPortMap.unlock();
        //CREO NUOVO FILE JSON PER IL NUOVO UTENTE
        try (FileWriter filejson = new FileWriter(path)) {
            filejson.write(user.toJSONString());
            filejson.flush();
            filejson.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }
        try{



            //SE IL FILE CON LE PORTE E' VUOTO CI SALVO DENTRO LA HASHMAP
            if(filePorts.length() == 0){

                boolean found = controlKnownPorts(port_UDP);
                if(found){
                    port_UDP++;
                    this.portMap.put(nickUtente,port_UDP);
                }
                FileOutputStream fos = new FileOutputStream(filePorts);
                ObjectOutputStream oos=new ObjectOutputStream(fos);

                oos.writeObject(this.portMap);
                oos.flush();
                oos.close();
                fos.close();
            }
            //ALTRIMENTI PRENDO IL FILE, MI SALVO LA SUA HASHMAP E SUCCESSIVAMENTE CI SCRIVO LA HASHMAP AGGIORNATA
            else{

                FileInputStream fis=new FileInputStream(filePorts);
                ObjectInputStream ois=new ObjectInputStream(fis);

                this.portMap = (HashMap<String, Integer>) ois.readObject();

                boolean found = controlKnownPorts(port_UDP);

                //SE LA PORTA E' UGUALE AD UNA GIA' NOTA, LA AUMENTO DI 1
                if(found){
                    port_UDP++;
                    this.portMap.put(nickUtente,port_UDP);
                }
                else {
                    this.portMap.put(nickUtente, port_UDP);
                }
                FileOutputStream fos = new FileOutputStream(filePorts);
                ObjectOutputStream oos=new ObjectOutputStream(fos);

                oos.writeObject(this.portMap);
                oos.flush();
                oos.close();
                fos.close();

                ois.close();
                fis.close();
            }




            FileOutputStream fos = new FileOutputStream(filePorts);
            ObjectOutputStream oos=new ObjectOutputStream(fos);

            oos.writeObject(this.portMap);
            oos.flush();
            oos.close();
            fos.close();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }

        return 0;
    }

    @Override
    //LOGIN
    public int login(String nickUtente, String password) throws RemoteException{
        /*
        return 0 == tutto ok
        return 1 == sei già loggato
        return 2 == password sbagliata
        return 3 == utente non registrato
        return 4 == nickUtente o password nulli
         */
        if (utentiLoggati.contains(nickUtente)) {
            //UTENTE GIA' LOGGATO
            return 1;
        }
        if(nickUtente == null || password == null){
            //UNO DEI DUE E' NULL
            return 4;
        }

        String current = nickUtente.concat(".json");

        File folder = new File("./Users");
        File[] ListOfFiles = folder.listFiles();
        for (int i = 0; i < ListOfFiles.length; i++) {

            if (ListOfFiles[i].getName().compareTo(current) == 0) {


                JSONParser parser = new JSONParser();
                try {
                    String path = pathUser.concat("/").concat(nickUtente).concat(".json");

                    Object obj = parser.parse(new FileReader(path));
                    JSONObject jsonObj = (JSONObject) obj;
                    String pass = (String) jsonObj.get("password");

                    if (pass.compareTo(password) == 0) {
                        //LOGIN AVVENUTO CON SUCCESSO
                        boolean acquireLock;

                        while(!(acquireLock = lockUtentiLoggati.tryLock())){
                            try {
                                wait(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        this.utentiLoggati.add(nickUtente);
                        lockUtentiLoggati.unlock();
                        return 0;
                    } else {
                        //PASSWORD SBAGLIATA
                        return 2;
                    }


                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                }
            }
        }

        //UTENTE NON REGISTRATO
        return 3;
    }

    @Override
    public int logout(String nickUtente) throws RemoteException{
        /*
        return 0 == utente rimosso dai loggati
        return 1 == utente non loggato
        return 2 == nickUtente è null
         */
        if(nickUtente == null){
           //UTENTE NULLO
            return 2;
        }
        if (utentiLoggati.contains(nickUtente)) {
            boolean acquireLock;
            while(!(acquireLock = lockUtentiLoggati.tryLock())){
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            utentiLoggati.remove(nickUtente);
            lockUtentiLoggati.unlock();
            return 0;
        } else {
            //UTENTE NON LOGGATO
            return 1;
        }
    }

    @Override
    public int aggiungi_amico(String nickUtente, String nickAmico) throws RemoteException{
        /*
        return 0 == tutto ok
        return 1 == non sei loggato
        return 2 == amico inesistente
        return 3 == siete già amici
        return 4 == nickUtente o nickAmico nulli
        return 5 == nickUtente uguale a nickAmico
         */
        if(nickUtente.compareTo(nickAmico) == 0){
            // NON SI PUO' AGGIUNGERE SE STESSI
            return 5;
        }
        if(nickUtente == null || nickAmico == null){
            //UTENTE O AMICO NULLI
            return 4;
        }
        if (!(utentiLoggati.contains(nickUtente))) {
            //UTENTE NON LOGGATO
            return 1;
        }
        boolean trovato = false;
        String current = nickAmico.concat(".json");
        File folder = new File("./Users");
        File[] ListOfFiles = folder.listFiles();
        for (int i = 0; i < ListOfFiles.length; i++) {
            if (ListOfFiles[i].getName().compareTo(current) == 0) {

                trovato = true;
                break;
            }
        }
        if (!trovato) {
            // AMICO INESISTENTE
            return 2;
        }


        try {
            //AGGIUNGO NICKAMICO ALLA LISTA AMICI DI NICKUTENTE
            JSONParser parser = new JSONParser();
            String path = pathUser.concat("/").concat(nickUtente).concat(".json");
            JSONObject jsonObj = new JSONObject();


            Object obj = parser.parse(new FileReader(path));
            JSONObject oldfile = (JSONObject) obj;
            ArrayList<String> listaAmici = (ArrayList<String>) oldfile.get("lista amici");
            if (listaAmici.contains(nickAmico)) {
                // SIETE GIA' AMICI
                return 3;
            }
            listaAmici.add(nickAmico);

            String pass = (String) oldfile.get("password");
            String user = (String) oldfile.get("nome");
            long punti = (long) oldfile.get("punteggio");


            jsonObj.put("nome", user);
            jsonObj.put("password", pass);
            jsonObj.put("punteggio", punti);
            jsonObj.put("lista amici", listaAmici);


            try (FileWriter filejson = new FileWriter(path)) {
                filejson.write(jsonObj.toJSONString());
                filejson.flush();
                filejson.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

            }


        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }

        try {
            //AGGIUNGO NICKUTENTE ALLA LISTA AMICI DI NICKAMICO
            JSONParser parseramico = new JSONParser();
            String path = pathUser.concat("/").concat(nickAmico).concat(".json");
            JSONObject jsonObj = new JSONObject();


            Object obj = parseramico.parse(new FileReader(path));
            JSONObject oldfile = (JSONObject) obj;
            ArrayList<String> listaAmici = (ArrayList<String>) oldfile.get("lista amici");
            if (listaAmici.contains(nickUtente)) {
                //SIETE GIA' AMICI
                return 3;
            }
            listaAmici.add(nickUtente);

            String pass = (String) oldfile.get("password");
            String user = (String) oldfile.get("nome");
            long punti = (long) oldfile.get("punteggio");


            jsonObj.put("nome", user);
            jsonObj.put("password", pass);
            jsonObj.put("punteggio", punti);
            jsonObj.put("lista amici", listaAmici);


            try (FileWriter filejson = new FileWriter(path)) {
                filejson.write(jsonObj.toJSONString());
                filejson.flush();
                filejson.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }
        //TUTTO OK
        return 0;
    }

    @Override
    public JSONObject lista_amici(String nickUtente) throws RemoteException{
        /*
        codice 1 = nickUtente nullo
        codice 2 = utente non loggato
         */
        if(nickUtente == null){
            //UTENTE NULLO
            JSONObject obj = new JSONObject();
            obj.put("messaggioErrore", "utente nullo");
            obj.put("codice",1);
            return obj;
        }
        if (!(utentiLoggati.contains(nickUtente))) {
            //UTENTE NON LOGGATO
            JSONObject obj = new JSONObject();
            obj.put("messaggioErrore", "utente non loggato");
            obj.put("codice",2);
            return obj;
        }
        try {
            //TIRO FUORI IL FILE DI NICKUTENTE E NE PRENDO LA LISTA AMICI
            JSONParser parser = new JSONParser();
            String path = pathUser.concat("/").concat(nickUtente).concat(".json");
            JSONObject jsonObj = new JSONObject();


            Object obj = parser.parse(new FileReader(path));
            JSONObject objnew = (JSONObject) obj;
            ArrayList<String> listaAmici = (ArrayList<String>) objnew.get("lista amici");


            jsonObj.put("lista amici", listaAmici);
            jsonObj.put("codice", 0);

            return jsonObj;
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }
        return null;
    }

    @Override
    public int sfida(String nickUtente, String nickAmico) throws RemoteException{
        /*
        return 1 = utente non loggato
        return 2 = amico non loggato
        return 3 = timeout di accettazione sfida scaduto
        return 4 = amico o utente nulli
        return 5 = amico già impegnato in una sfida
        return 6 = l'utente sfidato non è in lista amici
        return -1 = nickUtente uguale a nickAmico
         */
        if(nickUtente.compareTo(nickAmico) == 0){
            // NON SI PUO' SFIDARE SE STESSI
            return -1;
        }
        if(this.sfidaInCorso.contains(nickAmico)){
            //AMICO GIA' IMPEGNATO IN UNA SFIDA
            return 5;
        }


        if(nickAmico.length() == 0){
            //SE NON HO SCRITTO IL NOME DELL'AMICO
            return 4;
        }
        if (!(utentiLoggati.contains(nickUtente))) {
            //UTENTE NON LOGGATO
            return 1;
        }
        if (!(utentiLoggati.contains(nickAmico))) {
            // AMICO NON LOGGATO
            return 2;
        }
        //CONTROLLO CHE SIANO AMICI ATTRAVERSO LA FUNZIONE CONTROLLA AMICO
        DatagramSocket sock = null;
        if(controllaAmico(nickUtente, nickAmico)) {
            try {


                FileInputStream fis= new FileInputStream(filePorts);
                ObjectInputStream ois=new ObjectInputStream(fis);
                //PRENDO LE PORTE DAL FILE
                boolean acquireLock;
                while(!(acquireLock = lockPortMap.tryLock())){
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



                this.portMap = (HashMap<String, Integer>) ois.readObject();
                int PORT_TCP_THREAD_AMICO = portMap.get(nickAmico);
                int PORT_TCP_THREAD_UTENTE = portMap.get(nickUtente);
                lockPortMap.unlock();
                sock = new DatagramSocket(PORT_UDP);
                sock.setReuseAddress(true);

                //TEMPO DI ATTESA DI ARRIVO DEL PACCHETTO UDP
                sock.setSoTimeout(60000);
                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress IP = InetAddress.getByName("localhost");
                sendData = nickUtente.getBytes();

                //INVIO IL PACCHETTO CONTENENTE LA RICHIESTA DI SFIDA
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP, Math.toIntExact(PORT_TCP_THREAD_AMICO));
                sock.send(sendPacket);

                // ATTENDO LA RISPOSTA
                DatagramPacket receivePacketRes = new DatagramPacket(receiveData, receiveData.length);

                sock.receive(receivePacketRes);

                String receive = new String(receivePacketRes.getData(), 0, receivePacketRes.getLength());

                if(receive.compareTo("si") == 0){
                    //SE HA ACCETTATO AGGIORNO GLI UTENTI IN SFIDA E FACCIO PARTIRE UN THREAD SFIDA DALL'EXECUTOR
                    sendData = receive.getBytes();

                    DatagramPacket sendPacketConfirm = new DatagramPacket(sendData, sendData.length, IP, Math.toIntExact(PORT_TCP_THREAD_UTENTE));
                    sock.send(sendPacketConfirm);
                    boolean acquireLockS;
                    while(!(acquireLockS = lockSfidaInCorso.tryLock())){
                        try{
                            wait(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    this.sfidaInCorso.add(nickUtente);
                    this.sfidaInCorso.add(nickAmico);
                    lockSfidaInCorso.unlock();
                    //PREPARO GLI ARRAYLIST CONTENENTI LE PAROLE DA TRADURRE E LE CORRISPETTIVE TRADUZIONI
                    PrepareArrays();

                    Sfida sfida = new Sfida(nickAmico, this.translatedWords, this.arrWords, PORT_TCP_THREAD_AMICO, PORT_TCP_THREAD_UTENTE);
                    executor.execute(sfida);
                    //ATTENDO FINCHE' NON TERMINO LA SFIDA
                    while(!sfida.getSfidaTerminata()){
                        Thread.sleep(100);
                    }

                }
                else{
                    sendData = receive.getBytes();

                    DatagramPacket sendPacketNeg = new DatagramPacket(sendData, sendData.length, IP, Math.toIntExact(PORT_TCP_THREAD_UTENTE));
                    sock.send(sendPacketNeg);
                    sock.close();
                    return 0;
                }

                this.translatedWords.clear();
                this.arrWords.clear();
                boolean acquireLockS;
                while(!(acquireLockS = lockSfidaInCorso.tryLock())){
                    try{
                        wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



                this.sfidaInCorso.remove(nickAmico);
                this.sfidaInCorso.remove(nickUtente);
                lockSfidaInCorso.unlock();
                sock.close();
                return 0;





            } catch(SocketTimeoutException e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

                return 3;
            } catch (ClassNotFoundException | IOException e) {
                JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();

            } finally {
                if(sock != null) {
                    sock.close();
                }
            }


        }
        else return 6;
        return 0;
    }

    @Override
    public int mostra_punteggio(String nickUtente)throws RemoteException{
        /*
        return -1 == utente non loggato
        return -2 == utente nullo
         */

        if(nickUtente == null){
            //UTENTE NULLO
            return -2;
        }
        if (!(utentiLoggati.contains(nickUtente))) {
            //UTENTE NON LOGGATO
            return -1;
        }
        try {
            //PRENDO DAL FILE NICKUTENTE IL PUNTEGGIO
            JSONParser parser = new JSONParser();
            String path = pathUser.concat("/").concat(nickUtente).concat(".json");



            Object obj = parser.parse(new FileReader(path));
            JSONObject objnew = (JSONObject) obj;
            long punti = (long) objnew.get("punteggio");
            int puntiInt = Math.toIntExact(punti);
            return puntiInt;
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }
        return -1;
    }

    @Override
    public JSONObject mostra_classifica(String nickUtente)throws RemoteException{
        /*
        codice 1 = nickUtente nullo
        codice 2 = utente non loggato
         */
        if(nickUtente == null){
            //UTENTE NULLO
            JSONObject obj = new JSONObject();
            obj.put("messaggioErrore", "utente nullo");
            obj.put("codice", 1);
            return obj;

        }
        if (!(utentiLoggati.contains(nickUtente))) {
            //UTENTE NON LOGGATO
            JSONObject obj = new JSONObject();
            obj.put("messaggioErrore", "utente non loggato");
            obj.put("codice", 2);
            return obj;
        }
        HashMap<String, Long> map = new HashMap<>();



        try {
            //PRENDO LA LISTA AMICI DI NICKUTENTE E I LORO RISPETTIVI PUNTEGGI PER CREARE UNA CLASSIFICA
            JSONParser parser = new JSONParser();
            String path = pathUser.concat("/").concat(nickUtente).concat(".json");



            Object obj = parser.parse(new FileReader(path));
            JSONObject objnew = (JSONObject) obj;
            long punti = (long) objnew.get("punteggio");
            String name = (String) objnew.get("nome");
            map.put(name,punti);
            ArrayList<String> listaAmici = (ArrayList<String>) objnew.get("lista amici");
            for(int i = 0; i < listaAmici.size(); i++){
                JSONParser jsonParser = new JSONParser();
                String pathAmico = pathUser.concat("/").concat(listaAmici.get(i)).concat(".json");
                JSONObject objAmico = new JSONObject();

                Object objParse = jsonParser.parse(new FileReader(pathAmico));
                JSONObject objAmicoP = (JSONObject) objParse;
                long puntiAmico = (long) objAmicoP.get("punteggio");
                map.put(listaAmici.get(i), puntiAmico);
            }

            JSONObject classifica = new JSONObject();
            classifica.put("classifica", map);

            classifica.put("codice", 0);
            return classifica;


        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }
        return null;
    }
}