import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceRestaurant extends Remote {

    String getRestaurants() throws RemoteException;

    String getPlats(
        int idRestaurant
    ) throws RemoteException;

    String reserverTable(
        String nom,
        String prenom,
        int convives,
        String telephone,
        int idRestaurant,
        String date,
        String heure
    ) throws RemoteException;

    String creerCommande(
        int numReservation
    ) throws RemoteException;

    String ajouterPlatCommande(
        int numCommande,
        int numPlat,
        int quantite
    ) throws RemoteException;

    String getCommande(
        int numCommande
    ) throws RemoteException;
}