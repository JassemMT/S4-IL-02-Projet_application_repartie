package sae.s4.rmi.service;

import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

public class LancerServeur {

    public static void main(String[] args) {
        try {
            // Lecture du config.properties
            Properties props = new Properties();
            try (InputStream in = LancerServeur.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (in == null) throw new RuntimeException("config.properties introuvable");
                props.load(in);
            }

            // Hostname RMI configurable (pas d'IP codée en dur)
            String rmiHostname = props.getProperty("rmi.hostname", "127.0.0.1");
            System.setProperty("java.rmi.server.hostname", rmiHostname);

            // Paramètres optionnels : LancerServeur [user] [password]
            String dbUser = props.getProperty("db.user");
            String dbPassword = props.getProperty("db.password");

            if (args.length >= 1) dbUser = args[0];
            if (args.length >= 2) dbPassword = args[1];

            int port = Integer.parseInt(props.getProperty("rmi.port", "1099"));
            String rmiName = props.getProperty("rmi.name", "serviceRestaurant");

            // Création de l'annuaire et enregistrement du service
            Registry registry = LocateRegistry.createRegistry(port);
            ServiceRestaurantImpl service = new ServiceRestaurantImpl(dbUser, dbPassword);
            registry.rebind(rmiName, service);

            System.out.println("Service RMI '" + rmiName + "' démarré sur le port " + port);
            System.out.println("En attente de requêtes...");

        } catch (Exception e) {
            System.err.println("Erreur au démarrage : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
