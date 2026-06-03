import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class ProxyHTTP {
    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws java.io.IOException {
        byte[] response = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    public static void main(String[] args) {

        // --- Lecture du config.properties ---
        Properties props = new Properties();
        try (InputStream in = ProxyHTTP.class.getClassLoader()
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

        // registre RMI (comme LancerClient le fait)
        Registry registry = LocateRegistry.getRegistry(host, port);
        ServiceRestaurant service = (ServiceRestaurant) registry.lookup(rmiName);

        // Ouvrir un serveur HTTP sur un port (ex: 8080)
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);


        /*
        Un appel ressemble à ça : const res = await fetch("http://adresse-proxy:8080/restaurants");
        */


        // Pour chaque route HTTP, appeler le RMI et répondre en JSON
        server.createContext("/restaurants", exchange -> {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"status\":\"error\",\"message\":\"Méthode non autorisée\"}");
                    return;
                }

                String json = service.getRestaurants();
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"error\",\"message\":\"Erreur proxy restaurants\"}");
            } finally {
                exchange.close();
            }
        });


        server.createContext("/reserver", exchange -> {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"status\":\"error\",\"message\":\"Méthode non autorisée\"}");
                    return;
                }

                String body = new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();

                String nom = obj.get("nom").getAsString();
                String prenom = obj.get("prenom").getAsString();
                int convives = obj.get("convives").getAsInt();
                String tel = obj.get("telephone").getAsString();
                int idResto = obj.get("idRestaurant").getAsInt();
                String date = obj.get("date").getAsString();
                String heure = obj.get("heure").getAsString();

                String json = service.reserverTable(nom, prenom, convives, tel, idResto, date, heure);
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"status\":\"error\",\"message\":\"Erreur proxy réservation\"}");
            } finally {
                exchange.close();
            }
        });

        server.start();
        System.out.println("Proxy HTTP démarré sur le port 8080");
    }
}