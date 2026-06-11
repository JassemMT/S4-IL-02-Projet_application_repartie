import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface distante définissant les services RMI disponibles pour les restaurants.
 * Fournit des méthodes pour la consultation et la réservation.
 */
public interface ServiceRestaurant extends Remote {

    /**
     * Récupère la liste de tous les restaurants disponibles dans la base de données.
     * 
     * @return Une chaîne de caractères contenant la liste des restaurants au format JSON.
     * @throws RemoteException En cas d'erreur de communication réseau RMI.
     */
    public String getRestaurants()
            throws RemoteException;

    /**
     * Tente de réserver une table dans un restaurant donné pour un certain créneau.
     * 
     * @param nom Le nom de famille du client.
     * @param prenom Le prénom du client.
     * @param convives Le nombre de personnes pour la réservation.
     * @param telephone Le numéro de téléphone du client.
     * @param idRestaurant L'identifiant unique du restaurant.
     * @param date La date souhaitée au format YYYY-MM-DD.
     * @param heure L'heure souhaitée au format HH:MM.
     * @return Une réponse au format JSON contenant le statut ("ok" ou "error") et un message.
     * @throws RemoteException En cas d'erreur réseau RMI ou problème sur la base de données.
     */
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