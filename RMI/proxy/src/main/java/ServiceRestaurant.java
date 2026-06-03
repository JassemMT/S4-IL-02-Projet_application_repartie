import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceRestaurant extends Remote {

    public String getRestaurants()
            throws RemoteException;

    public String reserverTable(
        String nom,
        String prenom,
        int convives,
        String telephone,
        int idRestaurant,
        String date,
        String heure
    ) throws RemoteException;
}