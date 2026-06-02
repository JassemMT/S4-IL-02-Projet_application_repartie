import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.InputStream;
import java.util.Properties;

public class LancerClient {

    public static void main(String[] args) {
        // --- Lecture du config.properties ---
        Properties props = new Properties();
        try (InputStream in = LancerClient.class.getClassLoader()
                                                .getResourceAsStream("config.properties")) {
            if (in == null) throw new RuntimeException("config.properties introuvable");
            props.load(in);
        } catch (Exception e) {
            System.err.println("Erreur chargement config : " + e.getMessage());
            return;
        }

        String host    = props.getProperty("rmi.host");
        int    port    = Integer.parseInt(props.getProperty("rmi.port", "1099"));
        String rmiName = props.getProperty("rmi.name");

        try {
            // --- Connexion à l'annuaire RMI ---
            Registry registry = LocateRegistry.getRegistry(host, port);
            ServiceRestaurant service = (ServiceRestaurant) registry.lookup(rmiName);
            System.out.println("Connecté au service RMI '" + rmiName + "' sur " + host + ":" + port);

            // --- Test 1 : récupération des restaurants ---
            System.out.println("\n=== Test getRestaurants() ===");
            String restaurants = service.getRestaurants();
            System.out.println(restaurants);

            // --- Test 2 : réservation valide ---
            System.out.println("\n=== Test reserverTable() — cas nominal ===");
            String reponse = service.reserverTable(
                "Dupont", "Jean", 3, "0612345678", 1, "2025-06-15", "20:30"
            );
            System.out.println(reponse);

            // --- Test 3 : restaurant inexistant ---
            System.out.println("\n=== Test reserverTable() — restaurant inexistant ===");
            String reponse2 = service.reserverTable(
                "Martin", "Alice", 2, "0698765432", 999, "2025-06-16", "19:00"
            );
            System.out.println(reponse2);

            // --- Test 4 : champs manquants ---
            System.out.println("\n=== Test reserverTable() — champs manquants ===");
            String reponse3 = service.reserverTable(
                "", "Alice", 2, "0698765432", 1, "2025-06-16", "19:00"
            );
            System.out.println(reponse3);

            // --- Test 5 : convives invalides ---
            System.out.println("\n=== Test reserverTable() — convives invalides ===");
            String reponse4 = service.reserverTable(
                "Martin", "Alice", -1, "0698765432", 1, "2025-06-16", "19:00"
            );
            System.out.println(reponse4);

        } catch (Exception e) {
            System.err.println("Erreur client RMI : " + e.getMessage());
            e.printStackTrace();
        }
    }
}