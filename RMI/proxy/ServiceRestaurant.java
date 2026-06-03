import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceRestaurant extends Remote {
    public String getRestaurants() throws RemoteException;
    public String reserverTable(String nom, String prenom,
                         int convives, String telephone,
                         int idRestaurant,
                         String date,    // format "YYYY-MM-DD"
                         String heure
                    )   // format "HH:MM"
            throws RemoteException;
}