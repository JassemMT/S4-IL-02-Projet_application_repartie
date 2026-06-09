import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Proxy HTTP faisant la passerelle entre le navigateur et les services RMI.
 * Résout le problème CORS empêchant le navigateur d'accéder directement aux services.
 */
public class ProxyHTTP {

    /** Ajoute les headers CORS à toute réponse. */
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Gère le preflight CORS (OPTIONS). Retourne true si la requête était OPTIONS
     */
    private static boolean handleOptions(HttpExchange exchange) throws java.io.IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    /** Envoie une réponse JSON avec les headers CORS. */
    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws java.io.IOException {
        addCorsHeaders(exchange);
        byte[] response = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    /** Extrait la valeur d'un paramètre de requête GET (ex: "?idRestaurant=3"). */
    private static String getQueryParam(HttpExchange exchange, String param) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) return kv[1];
        }
        return null;
    }

    public static void main(String[] args) {

        // --- Chargement de la configuration ---
        Properties props = new Properties();
        try (InputStream in = ProxyHTTP.class.getClassLoader()
                                              .getResourceAsStream("config.properties")) {
            if (in == null) throw new RuntimeException("config.properties introuvable dans le classpath");
            props.load(in);
        } catch (Exception e) {
            System.err.println("Erreur chargement config : " + e.getMessage());
            return;
        }

        String rmiHost      = props.getProperty("rmi.host", "localhost");
        int    rmiPort      = Integer.parseInt(props.getProperty("rmi.port", "1099"));
        String rmiName      = props.getProperty("rmi.name", "serviceRestaurant");
        int    httpPort     = Integer.parseInt(props.getProperty("http.port", "8080"));
        String incidentsUrl = props.getProperty("incidents.url", "").trim();
        String iutProxyHost = props.getProperty("iut.proxy.host", "").trim();
        String iutProxyPort = props.getProperty("iut.proxy.port", "").trim();

        HttpClient httpClient;

        /*Explication :
        * Si les variables d'environnement iut.proxy.host et iut.proxy.port sont définies, cela signifie que nous sommes dans l'environnement de l'IUT
        * et que nous devons utiliser le proxy de l'IUT pour accéder à Internet (notamment pour l'API Grand Nancy).
        * Sinon, nous sommes probablement en local et pouvons faire les requêtes HTTP directement sans proxy.
        */
        if (!iutProxyHost.isEmpty() && !iutProxyPort.isEmpty()) {
            httpClient = HttpClient.newBuilder()
                .proxy(java.net.ProxySelector.of(new InetSocketAddress(iutProxyHost, Integer.parseInt(iutProxyPort))))
                .build();
        } else {
            httpClient = HttpClient.newHttpClient();
        }

        // --- Connexion au registre RMI ---
        ServiceRestaurant service;
        try {
            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            service = (ServiceRestaurant) registry.lookup(rmiName);
            System.out.println("Connecté au service RMI : " + rmiName + " @ " + rmiHost + ":" + rmiPort);
        } catch (Exception e) {
            System.err.println("Erreur connexion RMI : " + e.getMessage());
            return;
        }

        // --- Démarrage du serveur HTTP ---
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(httpPort), 0);
        } catch (Exception e) {
            System.err.println("Erreur démarrage serveur HTTP sur le port " + httpPort + " : " + e.getMessage());
            return;
        }

        // GET /restaurants — liste tous les restaurants (avec coordonnées GPS)
        server.createContext("/restaurants", exchange -> {
            try {
                if (handleOptions(exchange)) return;
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"status\":\"error\",\"message\":\"Méthode non autorisée\"}");
                    return;
                }
                String json = service.getRestaurants();
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                System.err.println("Erreur /restaurants : " + e.getMessage());
                try { sendJson(exchange, 500, "{\"status\":\"error\",\"message\":\"Erreur interne\"}"); } catch (Exception ignored) {}
            } finally {
                exchange.close();
            }
        });

        // GET /plats?idRestaurant=X — plats d'un restaurant
        server.createContext("/plats", exchange -> {
            try {
                if (handleOptions(exchange)) return;
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"status\":\"error\",\"message\":\"Méthode non autorisée\"}");
                    return;
                }
                String idParam = getQueryParam(exchange, "idRestaurant");
                if (idParam == null) {
                    sendJson(exchange, 400, "{\"status\":\"error\",\"message\":\"Paramètre idRestaurant manquant\"}");
                    return;
                }
                int idRestaurant = Integer.parseInt(idParam);
                String json = service.getPlats(idRestaurant);
                sendJson(exchange, 200, json);
            } catch (NumberFormatException e) {
                try { sendJson(exchange, 400, "{\"status\":\"error\",\"message\":\"idRestaurant invalide\"}"); } catch (Exception ignored) {}
            } catch (Exception e) {
                System.err.println("Erreur /plats : " + e.getMessage());
                try { sendJson(exchange, 500, "{\"status\":\"error\",\"message\":\"Erreur interne\"}"); } catch (Exception ignored) {}
            } finally {
                exchange.close();
            }
        });

        // POST /reserver — réserver une table dans un restaurant
        server.createContext("/reserver", exchange -> {
            try {
                if (handleOptions(exchange)) return;
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"status\":\"error\",\"message\":\"Méthode non autorisée\"}");
                    return;
                }
                String body = new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();

                String nom      = obj.get("nom").getAsString();
                String prenom   = obj.get("prenom").getAsString();
                int    convives = obj.get("convives").getAsInt();
                String tel      = obj.get("telephone").getAsString();
                int    idResto  = obj.get("idRestaurant").getAsInt();
                String date     = obj.get("date").getAsString();
                String heure    = obj.get("heure").getAsString();

                String json = service.reserverTable(nom, prenom, convives, tel, idResto, date, heure);
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                System.err.println("Erreur /reserver : " + e.getMessage());
                try { sendJson(exchange, 500, "{\"status\":\"error\",\"message\":\"Erreur interne\"}"); } catch (Exception ignored) {}
            } finally {
                exchange.close();
            }
        });

        // GET /incidents — incidents de circulation Grand Nancy (via HttpClient, contourn CORS)
        server.createContext("/incidents", exchange -> {
            try {
                if (handleOptions(exchange)) return;
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"status\":\"error\",\"message\":\"Méthode non autorisée\"}");
                    return;
                }
                if (incidentsUrl.isEmpty()) {
                    sendJson(exchange, 503, "{\"status\":\"error\",\"message\":\"URL incidents non configurée\"}");
                    return;
                }
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(incidentsUrl))
                    .GET()
                    .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    sendJson(exchange, 502, "{\"status\":\"error\",\"message\":\"Erreur API Grand Nancy : " + response.statusCode() + "\"}");
                    return;
                }
                sendJson(exchange, 200, response.body());
            } catch (Exception e) {
                System.err.println("Erreur /incidents : " + e.getMessage());
                try { sendJson(exchange, 500, "{\"status\":\"error\",\"message\":\"Erreur interne\"}"); } catch (Exception ignored) {}
            } finally {
                exchange.close();
            }
        });

        server.start();
        System.out.println("Proxy HTTP démarré sur le port " + httpPort);
    }
}
