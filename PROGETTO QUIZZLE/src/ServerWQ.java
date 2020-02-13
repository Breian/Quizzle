import org.json.simple.JSONObject;
import java.rmi.*;
public interface ServerWQ extends Remote{

    public int registra_utente(String nickUtente, String password, int port_UDP) throws RemoteException;

    public int login(String nickUtente, String password) throws RemoteException;

    public int logout(String nickUtente)throws RemoteException;

    public int aggiungi_amico(String nickUtente, String nickAmico)throws RemoteException;

    public JSONObject lista_amici(String nickUtente)throws RemoteException;

    public int sfida(String nickUtente, String nickAmico)throws RemoteException;

    public int mostra_punteggio(String nickUtente)throws RemoteException;

    public JSONObject mostra_classifica(String nickUtente)throws RemoteException;


}