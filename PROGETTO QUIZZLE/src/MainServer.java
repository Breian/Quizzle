import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MainServer {
    private static final int PORT_RMI = 6789;
    private static final int PORT_TCP = 1234;

    public static void main(String[] args) throws RemoteException {
        try {
            //DICHIARAZIONE SERVERQUIZZLE CONTENENTE I METODI E DEL REGISTRY RMI
            ServerQuizzle server = new ServerQuizzle();
            ServerWQ stub = (ServerWQ) UnicastRemoteObject.exportObject(server, 0);
            LocateRegistry.createRegistry(PORT_RMI);
            Registry r = LocateRegistry.getRegistry(PORT_RMI);
            r.rebind("QUIZZLE-SERVER", stub);

            ServerSocket serverTCP = new ServerSocket(PORT_TCP);

            while (true) {

                Socket socket = serverTCP.accept();
                //ASSEGNO UN CLIENTSERVICE AD OGNI CLIENT CHE SI COLLEGA
                ClientService service = new ClientService(socket, server);

                service.start();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "ERRORE : " + e.toString(), "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);

        }

    }
}