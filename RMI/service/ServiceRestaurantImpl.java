import java.io.InputStream;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ServiceRestaurantImpl extends UnicastRemoteObject implements ServiceRestaurant {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public ServiceRestaurantImpl() throws RemoteException {
        super();
        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) throw new RuntimeException("config.properties introuvable");
            props.load(in);
        } catch (Exception e) {
            throw new RemoteException("Erreur chargement config : " + e.getMessage());
        }

        dbUrl = props.getProperty("db.url");
        dbUser = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private static String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private static String jsonString(String value) {
        return "\"" + jsonEscape(value) + "\"";
    }

    private static String jsonError(String message) {
        return "{\"status\":\"error\",\"message\":" + jsonString(message) + "}";
    }

    private static String jsonOk(String message) {
        return "{\"status\":\"ok\",\"message\":" + jsonString(message) + "}";
    }

    private static int nextVal(Connection con, String sequence) throws SQLException {
        String sql = "SELECT " + sequence + ".NEXTVAL FROM DUAL";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static boolean lockTimeout(SQLException e) {
        return e.getErrorCode() == 30006;
    }

    /* =====================================================
       RESTAURANTS
       Lecture simple : aucun verrou
       ===================================================== */

    @Override
    public String getRestaurants() throws RemoteException {
        StringBuilder sb = new StringBuilder("[");
        String sql = "SELECT NUM_RESTAURANT, NOM, ADRESSE, LATITUDE, LONGITUDE "
                   + "FROM RESTAURANT ORDER BY NUM_RESTAURANT";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;

            while (rs.next()) {
                if (!first) sb.append(",");

                sb.append("{")
                  .append("\"id\":").append(rs.getInt("NUM_RESTAURANT")).append(",")
                  .append("\"nom\":").append(jsonString(rs.getString("NOM"))).append(",")
                  .append("\"adresse\":").append(jsonString(rs.getString("ADRESSE"))).append(",")
                  .append("\"description\":\"\",")
                  .append("\"latitude\":").append(rs.getDouble("LATITUDE")).append(",")
                  .append("\"longitude\":").append(rs.getDouble("LONGITUDE")).append(",")
                  .append("\"lat\":").append(rs.getDouble("LATITUDE")).append(",")
                  .append("\"lng\":").append(rs.getDouble("LONGITUDE"))
                  .append("}");

                first = false;
            }

        } catch (SQLException e) {
            System.err.println("Erreur getRestaurants : " + e.getMessage());
            return jsonError("Erreur base de données");
        }

        return sb.append("]").toString();
    }

    /* =====================================================
       PLATS D'UN RESTAURANT
       Lecture simple : aucun verrou
       ===================================================== */

    @Override
    public String getPlats(int idRestaurant) throws RemoteException {
        StringBuilder sb = new StringBuilder("[");
        String sql = "SELECT NUM_PLAT, LIBELLE, PRIX_UNITAIRE, QTE_STOCKEE "
                   + "FROM PLAT WHERE NUM_RESTAURANT = ? ORDER BY NUM_PLAT";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRestaurant);

            try (ResultSet rs = ps.executeQuery()) {
                boolean first = true;

                while (rs.next()) {
                    if (!first) sb.append(",");

                    sb.append("{")
                      .append("\"id\":").append(rs.getInt("NUM_PLAT")).append(",")
                      .append("\"libelle\":").append(jsonString(rs.getString("LIBELLE"))).append(",")
                      .append("\"prixUnitaire\":").append(rs.getBigDecimal("PRIX_UNITAIRE")).append(",")
                      .append("\"quantiteStockee\":").append(rs.getInt("QTE_STOCKEE"))
                      .append("}");

                    first = false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur getPlats : " + e.getMessage());
            return jsonError("Erreur base de données");
        }

        return sb.append("]").toString();
    }

    /* =====================================================
       RÉSERVATION
       Verrou : CRENEAU_TABLE
       ===================================================== */

    @Override
    public String reserverTable(String nom, String prenom, int convives,
                                String telephone, int idRestaurant,
                                String date, String heure) throws RemoteException {

        if (nom == null || nom.isBlank()
                || prenom == null || prenom.isBlank()
                || telephone == null || telephone.isBlank()
                || date == null || date.isBlank()
                || heure == null || heure.isBlank()) {
            return jsonError("Champs obligatoires manquants");
        }

        if (convives <= 0) return jsonError("Nombre de convives invalide");

        LocalDateTime dateHeure;

        try {
            LocalDate dateValue = LocalDate.parse(date, DATE_FORMAT);
            LocalTime timeValue = LocalTime.parse(heure, TIME_FORMAT);
            dateHeure = LocalDateTime.of(dateValue, timeValue);
        } catch (DateTimeParseException e) {
            return jsonError("Format invalide : date YYYY-MM-DD et heure HH:MM");
        }

        String sqlCandidates =
            "SELECT ct.NUM_CRENEAU "
          + "FROM CRENEAU_TABLE ct "
          + "JOIN TABLE_RESTAURANT tr ON tr.NUM_TABLE = ct.NUM_TABLE "
          + "WHERE tr.NUM_RESTAURANT = ? "
          + "AND ct.STATUT = 'LIBRE' "
          + "AND ct.DEBUT <= ? "
          + "AND ct.FIN > ? "
          + "AND tr.NB_PLACES >= ? "
          + "ORDER BY tr.NB_PLACES, ct.NUM_CRENEAU";

        String sqlLockSlot =
            "SELECT ct.STATUT, ct.DEBUT, ct.FIN, r.NOM "
          + "FROM CRENEAU_TABLE ct "
          + "JOIN TABLE_RESTAURANT tr ON tr.NUM_TABLE = ct.NUM_TABLE "
          + "JOIN RESTAURANT r ON r.NUM_RESTAURANT = tr.NUM_RESTAURANT "
          + "WHERE ct.NUM_CRENEAU = ? "
          + "AND tr.NUM_RESTAURANT = ? "
          + "AND tr.NB_PLACES >= ? "
          + "FOR UPDATE OF ct.STATUT WAIT 5";

        String sqlInsert =
            "INSERT INTO RESERVATION "
          + "(NUM_RESERVATION, NUM_CRENEAU, NOM_CLIENT, PRENOM_CLIENT, TELEPHONE, NB_CONVIVES) "
          + "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlUpdate =
            "UPDATE CRENEAU_TABLE SET STATUT = 'RESERVEE' WHERE NUM_CRENEAU = ?";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                Timestamp timestamp = Timestamp.valueOf(dateHeure);
                List<Integer> candidates = new ArrayList<>();

                try (PreparedStatement ps = con.prepareStatement(sqlCandidates)) {
                    ps.setInt(1, idRestaurant);
                    ps.setTimestamp(2, timestamp);
                    ps.setTimestamp(3, timestamp);
                    ps.setInt(4, convives);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) candidates.add(rs.getInt("NUM_CRENEAU"));
                    }
                }

                Integer numCreneau = null;
                String nomRestaurant = null;
                Timestamp debut = null;
                Timestamp fin = null;

                for (Integer candidate : candidates) {
                    try (PreparedStatement ps = con.prepareStatement(sqlLockSlot)) {
                        ps.setInt(1, candidate);
                        ps.setInt(2, idRestaurant);
                        ps.setInt(3, convives);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && "LIBRE".equals(rs.getString("STATUT"))) {
                                numCreneau = candidate;
                                nomRestaurant = rs.getString("NOM");
                                debut = rs.getTimestamp("DEBUT");
                                fin = rs.getTimestamp("FIN");
                                break;
                            }
                        }

                    } catch (SQLException e) {
                        if (!lockTimeout(e)) throw e;
                    }
                }

                if (numCreneau == null) {
                    con.rollback();
                    return jsonError("Aucun créneau disponible");
                }

                int numReservation = nextVal(con, "SEQ_RESERVATION");

                try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                    ps.setInt(1, numReservation);
                    ps.setInt(2, numCreneau);
                    ps.setString(3, nom);
                    ps.setString(4, prenom);
                    ps.setString(5, telephone);
                    ps.setInt(6, convives);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, numCreneau);
                    ps.executeUpdate();
                }

                con.commit();

                return "{"
                    + "\"status\":\"ok\","
                    + "\"message\":\"Réservation confirmée\","
                    + "\"idReservation\":" + numReservation + ","
                    + "\"restaurant\":" + jsonString(nomRestaurant) + ","
                    + "\"convives\":" + convives + ","
                    + "\"debut\":" + jsonString(debut.toString()) + ","
                    + "\"fin\":" + jsonString(fin.toString())
                    + "}";

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Erreur reserverTable : " + e.getMessage());
            return jsonError("Réservation impossible");
        }
    }

    /* =====================================================
       CRÉER UNE COMMANDE
       Verrou : RESERVATION
       ===================================================== */

    @Override
    public String creerCommande(int numReservation) throws RemoteException {
        String sqlLock =
            "SELECT NUM_RESERVATION FROM RESERVATION "
          + "WHERE NUM_RESERVATION = ? FOR UPDATE WAIT 5";

        String sqlInsert =
            "INSERT INTO COMMANDE "
          + "(NUM_COMMANDE, NUM_RESERVATION, MONTANT, STATUT) "
          + "VALUES (?, ?, 0, 'EN_COURS')";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement(sqlLock)) {
                    ps.setInt(1, numReservation);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return jsonError("Réservation introuvable");
                        }
                    }
                }

                int numCommande = nextVal(con, "SEQ_COMMANDE");

                try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                    ps.setInt(1, numCommande);
                    ps.setInt(2, numReservation);
                    ps.executeUpdate();
                }

                con.commit();

                return "{"
                    + "\"status\":\"ok\","
                    + "\"message\":\"Commande créée\","
                    + "\"idCommande\":" + numCommande
                    + "}";

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Erreur creerCommande : " + e.getMessage());
            return jsonError("Création de commande impossible");
        }
    }

    /* =====================================================
       AJOUTER UN PLAT À UNE COMMANDE
       Verrous : COMMANDE puis PLAT
       ===================================================== */

    @Override
    public String ajouterPlatCommande(int numCommande, int numPlat, int quantite)
            throws RemoteException {

        if (quantite <= 0) return jsonError("Quantité invalide");

        String sqlLockCommande =
            "SELECT c.STATUT, tr.NUM_RESTAURANT "
          + "FROM COMMANDE c "
          + "JOIN RESERVATION r ON r.NUM_RESERVATION = c.NUM_RESERVATION "
          + "JOIN CRENEAU_TABLE ct ON ct.NUM_CRENEAU = r.NUM_CRENEAU "
          + "JOIN TABLE_RESTAURANT tr ON tr.NUM_TABLE = ct.NUM_TABLE "
          + "WHERE c.NUM_COMMANDE = ? "
          + "FOR UPDATE OF c.STATUT WAIT 5";

        String sqlLockPlat =
            "SELECT NUM_RESTAURANT, PRIX_UNITAIRE, QTE_STOCKEE "
          + "FROM PLAT WHERE NUM_PLAT = ? FOR UPDATE WAIT 5";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                int restaurantCommande;

                try (PreparedStatement ps = con.prepareStatement(sqlLockCommande)) {
                    ps.setInt(1, numCommande);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return jsonError("Commande introuvable");
                        }

                        if (!"EN_COURS".equals(rs.getString("STATUT"))) {
                            con.rollback();
                            return jsonError("Commande non modifiable");
                        }

                        restaurantCommande = rs.getInt("NUM_RESTAURANT");
                    }
                }

                int restaurantPlat;
                int stock;
                BigDecimal prix;

                try (PreparedStatement ps = con.prepareStatement(sqlLockPlat)) {
                    ps.setInt(1, numPlat);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return jsonError("Plat introuvable");
                        }

                        restaurantPlat = rs.getInt("NUM_RESTAURANT");
                        prix = rs.getBigDecimal("PRIX_UNITAIRE");
                        stock = rs.getInt("QTE_STOCKEE");
                    }
                }

                if (restaurantCommande != restaurantPlat) {
                    con.rollback();
                    return jsonError("Le plat ne correspond pas au restaurant");
                }

                if (stock < quantite) {
                    con.rollback();
                    return jsonError("Stock insuffisant");
                }

                String sqlStock =
                    "UPDATE PLAT SET QTE_STOCKEE = QTE_STOCKEE - ? WHERE NUM_PLAT = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                    ps.setInt(1, quantite);
                    ps.setInt(2, numPlat);
                    ps.executeUpdate();
                }

                String sqlUpdateContient =
                    "UPDATE CONTIENT SET QUANTITE = QUANTITE + ? "
                  + "WHERE NUM_COMMANDE = ? AND NUM_PLAT = ?";

                int lignesModifiees;

                try (PreparedStatement ps = con.prepareStatement(sqlUpdateContient)) {
                    ps.setInt(1, quantite);
                    ps.setInt(2, numCommande);
                    ps.setInt(3, numPlat);
                    lignesModifiees = ps.executeUpdate();
                }

                if (lignesModifiees == 0) {
                    String sqlInsertContient =
                        "INSERT INTO CONTIENT "
                      + "(NUM_COMMANDE, NUM_PLAT, QUANTITE, PRIX_UNITAIRE_COMMANDE) "
                      + "VALUES (?, ?, ?, ?)";

                    try (PreparedStatement ps = con.prepareStatement(sqlInsertContient)) {
                        ps.setInt(1, numCommande);
                        ps.setInt(2, numPlat);
                        ps.setInt(3, quantite);
                        ps.setBigDecimal(4, prix);
                        ps.executeUpdate();
                    }
                }

                String sqlMontant =
                    "UPDATE COMMANDE SET MONTANT = ("
                  + "SELECT NVL(SUM(QUANTITE * PRIX_UNITAIRE_COMMANDE), 0) "
                  + "FROM CONTIENT WHERE NUM_COMMANDE = ?"
                  + ") WHERE NUM_COMMANDE = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlMontant)) {
                    ps.setInt(1, numCommande);
                    ps.setInt(2, numCommande);
                    ps.executeUpdate();
                }

                con.commit();

                return jsonOk("Plat ajouté à la commande");

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Erreur ajouterPlatCommande : " + e.getMessage());
            return jsonError("Ajout du plat impossible");
        }
    }

    /* =====================================================
       RÉCUPÉRER UNE COMMANDE
       Lecture simple : aucun verrou
       ===================================================== */

    @Override
    public String getCommande(int numCommande) throws RemoteException {
        String sqlCommande =
            "SELECT NUM_COMMANDE, NUM_RESERVATION, DATE_COMMANDE, MONTANT, STATUT "
          + "FROM COMMANDE WHERE NUM_COMMANDE = ?";

        String sqlPlats =
            "SELECT p.NUM_PLAT, p.LIBELLE, c.QUANTITE, c.PRIX_UNITAIRE_COMMANDE "
          + "FROM CONTIENT c JOIN PLAT p ON p.NUM_PLAT = c.NUM_PLAT "
          + "WHERE c.NUM_COMMANDE = ? ORDER BY p.NUM_PLAT";

        try (Connection con = getConnection();
             PreparedStatement psCommande = con.prepareStatement(sqlCommande)) {

            psCommande.setInt(1, numCommande);

            try (ResultSet rsCommande = psCommande.executeQuery()) {
                if (!rsCommande.next()) return jsonError("Commande introuvable");

                StringBuilder json = new StringBuilder();

                json.append("{")
                    .append("\"idCommande\":").append(rsCommande.getInt("NUM_COMMANDE")).append(",")
                    .append("\"idReservation\":").append(rsCommande.getInt("NUM_RESERVATION")).append(",")
                    .append("\"dateCommande\":")
                    .append(jsonString(rsCommande.getTimestamp("DATE_COMMANDE").toString())).append(",")
                    .append("\"montant\":").append(rsCommande.getBigDecimal("MONTANT")).append(",")
                    .append("\"statut\":").append(jsonString(rsCommande.getString("STATUT"))).append(",")
                    .append("\"plats\":[");

                try (PreparedStatement psPlats = con.prepareStatement(sqlPlats)) {
                    psPlats.setInt(1, numCommande);

                    try (ResultSet rsPlats = psPlats.executeQuery()) {
                        boolean first = true;

                        while (rsPlats.next()) {
                            if (!first) json.append(",");

                            json.append("{")
                                .append("\"idPlat\":").append(rsPlats.getInt("NUM_PLAT")).append(",")
                                .append("\"libelle\":").append(jsonString(rsPlats.getString("LIBELLE"))).append(",")
                                .append("\"quantite\":").append(rsPlats.getInt("QUANTITE")).append(",")
                                .append("\"prixUnitaire\":")
                                .append(rsPlats.getBigDecimal("PRIX_UNITAIRE_COMMANDE"))
                                .append("}");

                            first = false;
                        }
                    }
                }

                return json.append("]}").toString();
            }

        } catch (SQLException e) {
            System.err.println("Erreur getCommande : " + e.getMessage());
            return jsonError("Erreur base de données");
        }
    }
}