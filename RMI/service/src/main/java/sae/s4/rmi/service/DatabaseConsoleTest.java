package sae.s4.rmi.service;

import java.rmi.RemoteException;

/**
 * Script de test en mode console pour la base de données.
 * Ne dépend pas de JUnit pour ne pas bloquer 'mvn clean package'.
 */
public class DatabaseConsoleTest {

    private static int testsPasses = 0;
    private static int testsEchoues = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("= DÉMARRAGE DE LA BATTERIE DE TESTS (CONSOLE)   =");
        System.out.println("=================================================\n");

        String dbUser = null;
        String dbPassword = null;

        if (args.length >= 2) {
            dbUser = args[0];
            dbPassword = args[1];
            System.out.println("Authentification : Identifiants fournis en paramètres.");
        } else {
            System.out.println("Authentification : config.properties (aucun paramètre fourni).");
        }

        final ServiceRestaurantImpl service;
        try {
            service = new ServiceRestaurantImpl(dbUser, dbPassword);
        } catch (RemoteException e) {
            System.err.println("[ERREUR] Impossible d'initialiser le service : " + e.getMessage());
            return;
        }

        System.out.println("\n-------------------------------------------------");
        System.out.println(" EXÉCUTION DES TESTS");
        System.out.println("-------------------------------------------------\n");

        // --- TEST 1 : Vérification de la liste des restaurants ---
        runTest("Test 1 : getRestaurants() renvoie bien un tableau JSON contenant les 5 établissements", () -> {
            String json = service.getRestaurants();
            assertCondition(json != null && json.startsWith("[") && json.endsWith("]"), "Le retour doit être un JSON Array", json);
            assertCondition(json.contains("L'Excelsior"), "Manque L'Excelsior", json);
            assertCondition(json.contains("Le Grand Café Foy"), "Manque Le Grand Café Foy", json);
            assertCondition(json.contains("Le Bouche à Oreille"), "Manque Le Bouche à Oreille", json);
            assertCondition(json.contains("Le Capucin Gourmand"), "Manque Le Capucin Gourmand", json);
            assertCondition(json.contains("La Table du Bon Roi Stanislas"), "Manque La Table du Bon Roi", json);
        });

        // --- TEST 2 : Réservation valide ---
        runTest("Test 2 : Réservation valide au Capucin Gourmand (id=4, 2 places, 21h00)", () -> {
            String json = service.reserverTable("Dupont", "Jean", 2, "0601020304", 4, "2026-06-10", "21:00");
            assertCondition(json != null && json.contains("\"status\":\"ok\""), "La réservation doit réussir", json);
        });

        // --- TEST 3 : Trop de convives ---
        runTest("Test 3 : Échec si le nombre de convives dépasse la capacité de la plus grande table (10 personnes à L'Excelsior)", () -> {
            String json = service.reserverTable("Test", "TropGros", 10, "0600000000", 1, "2026-06-10", "19:00");
            assertCondition(json != null && json.contains("\"status\":\"error\"") && json.contains("Aucun cr"), "Doit renvoyer une erreur de créneau introuvable", json);
        });

        // --- TEST 4 : Restaurant inexistant ---
        runTest("Test 4 : Échec pour un restaurant qui n'existe pas (id=99)", () -> {
            String json = service.reserverTable("Test", "Inexistant", 2, "0600000000", 99, "2026-06-10", "19:00");
            assertCondition(json != null && json.contains("\"status\":\"error\"") && json.contains("introuvable"), "Doit renvoyer restaurant introuvable", json);
        });

        // --- TEST 5 : Format de date incorrect ---
        runTest("Test 5 : Échec pour un format de date invalide (10-06-2026 au lieu de YYYY-MM-DD)", () -> {
            String json = service.reserverTable("Test", "Date", 2, "0600000000", 1, "10-06-2026", "19:00");
            assertCondition(json != null && json.contains("\"status\":\"error\"") && json.contains("Format invalide"), "Doit rejeter la date", json);
        });

        // --- TEST 6 : Format d'heure incorrect ---
        runTest("Test 6 : Échec pour un format d'heure invalide (19h00 au lieu de 19:00)", () -> {
            String json = service.reserverTable("Test", "Heure", 2, "0600000000", 1, "2026-06-10", "19h00");
            assertCondition(json != null && json.contains("\"status\":\"error\"") && json.contains("Format invalide"), "Doit rejeter l'heure", json);
        });

        // --- TEST 7 : Champ manquant ---
        runTest("Test 7 : Échec si le nom est vide", () -> {
            String json = service.reserverTable("", "Prenom", 2, "0600000000", 1, "2026-06-10", "19:00");
            assertCondition(json != null && json.contains("\"status\":\"error\"") && json.contains("obligatoires"), "Doit signaler les champs obligatoires", json);
        });

        // --- TEST 8 : Nombre de convives négatif ou nul ---
        runTest("Test 8 : Échec si le nombre de convives est <= 0", () -> {
            String json = service.reserverTable("Test", "Negatif", 0, "0600000000", 1, "2026-06-10", "19:00");
            assertCondition(json != null && json.contains("\"status\":\"error\"") && json.contains("invalide"), "Doit rejeter 0 convive", json);
        });

        // --- TEST 9 : Surclassement de table et gestion de concurrence ---
        runTest("Test 9 : Le Bouche à Oreille (id=3) n'a qu'une table de 2 et une table de 4. Test d'épuisement des tables", () -> {
            // Réservation 1 : 2 personnes. Va prendre la table de 2.
            String json1 = service.reserverTable("Test", "Table1", 2, "0601010101", 3, "2026-06-10", "19:00");
            assertCondition(json1 != null && json1.contains("\"status\":\"ok\""), "La 1ère réservation de 2 personnes doit réussir", json1);
            
            // Réservation 2 : 2 personnes encore. La table de 2 est prise, l'algorithme doit proposer la table de 4.
            String json2 = service.reserverTable("Test", "Table2", 2, "0602020202", 3, "2026-06-10", "19:00");
            assertCondition(json2 != null && json2.contains("\"status\":\"ok\""), "La 2ème réservation doit réussir en utilisant la table de 4", json2);

            // Réservation 3 : 2 personnes encore. Les deux tables sont prises, ça doit échouer.
            String json3 = service.reserverTable("Test", "Table3", 2, "0603030303", 3, "2026-06-10", "19:00");
            assertCondition(json3 != null && json3.contains("\"status\":\"error\""), "La 3ème réservation doit échouer (plus aucune table dispo)", json3);
        });

        // --- RÉSUMÉ ---
        System.out.println("-------------------------------------------------");
        System.out.println(" BILAN DES TESTS");
        System.out.println("-------------------------------------------------");
        System.out.println("Tests PASSÉS  : " + testsPasses);
        System.out.println("Tests ÉCHOUÉS : " + testsEchoues);
        
        if (testsEchoues == 0) {
            System.out.println("\n=> PARFAIT ! Tous les tests fonctionnent correctement.");
        } else {
            System.out.println("\n=> ATTENTION ! Il y a des erreurs à vérifier.");
        }
    }

    /**
     * Interface fonctionnelle pour isoler l'exécution d'un test.
     */
    interface TestRunnable {
        void run() throws Exception;
    }

    /**
     * Exécute un test avec gestion standard des succès/échecs et affichage.
     */
    private static void runTest(String testName, TestRunnable testCode) {
        System.out.println("⏳ " + testName);
        try {
            testCode.run();
            System.out.println("   [✓] SUCCÈS");
            testsPasses++;
        } catch (AssertionException e) {
            System.out.println("   [X] ÉCHEC d'assertion : " + e.getMessage());
            testsEchoues++;
        } catch (Exception e) {
            System.out.println("   [X] ERREUR inattendue : " + e.getMessage());
            e.printStackTrace(System.out);
            testsEchoues++;
        }
        System.out.println();
    }

    /**
     * Méthode d'assertion simple.
     */
    private static void assertCondition(boolean condition, String errorMessage, String actualJson) throws AssertionException {
        if (!condition) {
            throw new AssertionException(errorMessage + " | Résultat obtenu : " + actualJson);
        }
    }

    /**
     * Exception spécifique pour nos assertions personnalisées.
     */
    private static class AssertionException extends Exception {
        public AssertionException(String message) {
            super(message);
        }
    }
}
