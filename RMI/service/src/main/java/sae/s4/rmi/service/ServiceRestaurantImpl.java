package sae.s4.rmi.service;

import sae.s4.rmi.common.ServiceRestaurant;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implémentation du service RMI {@link ServiceRestaurant}.
 * Fournit l'accès à la base de données Oracle pour la consultation
 * des restaurants et la réservation de tables.
 *
 * <p>Chaque appel de méthode ouvre une connexion JDBC dédiée
 * (pas de pool), adaptée à la charge d'un projet universitaire.</p>
 *
 * @see ServiceRestaurant
 */
public class ServiceRestaurantImpl extends UnicastRemoteObject implements ServiceRestaurant {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    /**
     * Constructeur par défaut. Les identifiants de la base de données
     * sont lus depuis {@code config.properties}.
     *
     * @throws RemoteException si l'exportation de l'objet distant échoue.
     */
    public ServiceRestaurantImpl() throws RemoteException {
        this(null, null);
    }

    /**
     * Constructeur avec surcharge optionnelle des identifiants.
     *
     * @param dbUserOverride     utilisateur Oracle (ou {@code null} pour utiliser la config).
     * @param dbPasswordOverride mot de passe Oracle (ou {@code null} pour utiliser la config).
     * @throws RemoteException si l'exportation de l'objet distant échoue
     *                         ou si la configuration est introuvable.
     */
    public ServiceRestaurantImpl(String dbUserOverride, String dbPasswordOverride) throws RemoteException {
        super();
        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (in == null) {
                throw new RuntimeException("config.properties introuvable");
            }

            props.load(in);

        } catch (Exception e) {
            throw new RemoteException(
                "Erreur chargement config : " + e.getMessage()
            );
        }

        dbUrl = props.getProperty("db.url");

        // Les paramètres CLI surchargent le fichier de config
        dbUser = (dbUserOverride != null && !dbUserOverride.isBlank())
                ? dbUserOverride
                : props.getProperty("db.user");

        dbPassword = (dbPasswordOverride != null && !dbPasswordOverride.isBlank())
                ? dbPasswordOverride
                : props.getProperty("db.password");
    }

    /**
     * Ouvre une nouvelle connexion JDBC vers la base Oracle.
     *
     * @return une connexion JDBC active.
     * @throws SQLException si la connexion échoue.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Échappe les caractères spéciaux pour une inclusion dans une chaîne JSON.
     *
     * @param value la valeur à échapper (peut être {@code null}).
     * @return la valeur échappée, ou une chaîne vide si {@code null}.
     */
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
        return "{\"status\":\"error\",\"message\":"
                + jsonString(message) + "}";
    }

    /**
     * Vérifie si l'exception SQL correspond à un timeout de verrou Oracle (ORA-30006).
     *
     * @param e l'exception SQL à tester.
     * @return {@code true} si c'est un timeout de verrou.
     */
    private static boolean lockTimeout(SQLException e) {
        return e.getErrorCode() == 30006;
    }

    /**
     * Génère le prochain identifiant de réservation
     * via la séquence Oracle {@code SEQ_RESERVATION}.
     *
     * @param con la connexion JDBC active (dans une transaction).
     * @return le prochain numéro de réservation.
     * @throws SQLException si la requête échoue.
     */
    private static int nextReservationId(Connection con)
            throws SQLException {

        String sql = "SELECT SEQ_RESERVATION.NEXTVAL FROM DUAL";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }

    private static boolean restaurantExiste(
        Connection con,
        int idRestaurant
    ) throws SQLException {

        String sql =
            "SELECT NUM_RESTAURANT "
          + "FROM RESTAURANT "
          + "WHERE NUM_RESTAURANT = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRestaurant);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* =====================================================
       RÉCUPÉRER LES RESTAURANTS
       Lecture simple : aucun verrou nécessaire
       ===================================================== */

    @Override
    public String getRestaurants() throws RemoteException {
        StringBuilder json = new StringBuilder("[");

        String sql =
            "SELECT NUM_RESTAURANT, NOM, ADRESSE, "
          + "LATITUDE, LONGITUDE "
          + "FROM RESTAURANT "
          + "ORDER BY NUM_RESTAURANT";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");

                json.append("{")
                    .append("\"id\":")
                    .append(rs.getInt("NUM_RESTAURANT"))
                    .append(",\"nom\":")
                    .append(jsonString(rs.getString("NOM")))
                    .append(",\"adresse\":")
                    .append(jsonString(rs.getString("ADRESSE")))
                    .append(",\"lat\":")
                    .append(rs.getDouble("LATITUDE"))
                    .append(",\"lng\":")
                    .append(rs.getDouble("LONGITUDE"))
                    .append("}");

                first = false;
            }

        } catch (SQLException e) {
            System.err.println(
                "Erreur getRestaurants : " + e.getMessage()
            );

            return jsonError("Erreur base de données");
        }

        return json.append("]").toString();
    }

    /* =====================================================
       RÉSERVER UNE TABLE
       Verrou principal : CRENEAU_TABLE
       ===================================================== */

    @Override
    public String reserverTable(
        String nom,
        String prenom,
        int convives,
        String telephone,
        int idRestaurant,
        String date,
        String heure
    ) throws RemoteException {

        if (nom == null || nom.isBlank()
                || prenom == null || prenom.isBlank()
                || telephone == null || telephone.isBlank()
                || date == null || date.isBlank()
                || heure == null || heure.isBlank()) {

            return jsonError("Champs obligatoires manquants");
        }

        if (convives <= 0) {
            return jsonError("Nombre de convives invalide");
        }

        LocalDateTime dateHeure;

        try {
            dateHeure = LocalDateTime.of(
                LocalDate.parse(date, DATE_FORMAT),
                LocalTime.parse(heure, TIME_FORMAT)
            );

        } catch (DateTimeParseException e) {
            return jsonError(
                "Format invalide : date YYYY-MM-DD et heure HH:MM"
            );
        }

        /*
            Cherche les créneaux possibles.

            Les tables les plus petites sont essayées
            en premier pour ne pas gaspiller une grande table.
        */

        String sqlCandidates =
            "SELECT ct.NUM_CRENEAU "
          + "FROM CRENEAU_TABLE ct "
          + "JOIN TABLE_RESTAURANT tr "
          + "ON tr.NUM_TABLE = ct.NUM_TABLE "
          + "WHERE tr.NUM_RESTAURANT = ? "
          + "AND ct.STATUT = 'LIBRE' "
          + "AND ct.DEBUT <= ? "
          + "AND ct.FIN > ? "
          + "AND tr.NB_PLACES >= ? "
          + "ORDER BY tr.NB_PLACES, ct.NUM_CRENEAU";

        /*
            Verrouille précisément un créneau.

            WAIT 5 :
            attendre maximum 5 secondes si un autre
            client réserve déjà ce créneau.
        */

        String sqlLock =
            "SELECT ct.STATUT, ct.DEBUT, ct.FIN, r.NOM "
          + "FROM CRENEAU_TABLE ct "
          + "JOIN TABLE_RESTAURANT tr "
          + "ON tr.NUM_TABLE = ct.NUM_TABLE "
          + "JOIN RESTAURANT r "
          + "ON r.NUM_RESTAURANT = tr.NUM_RESTAURANT "
          + "WHERE ct.NUM_CRENEAU = ? "
          + "AND tr.NUM_RESTAURANT = ? "
          + "AND tr.NB_PLACES >= ? "
          + "AND ct.DEBUT <= ? "
          + "AND ct.FIN > ? "
          + "FOR UPDATE OF ct.STATUT WAIT 5";

        String sqlInsert =
            "INSERT INTO RESERVATION "
          + "(NUM_RESERVATION, NUM_CRENEAU, "
          + "NOM_CLIENT, PRENOM_CLIENT, "
          + "TELEPHONE, NB_CONVIVES) "
          + "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlUpdate =
            "UPDATE CRENEAU_TABLE "
          + "SET STATUT = 'RESERVEE' "
          + "WHERE NUM_CRENEAU = ?";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                if (!restaurantExiste(con, idRestaurant)) {
                    con.rollback();
                    return jsonError("Restaurant introuvable");
                }

                Timestamp timestamp = Timestamp.valueOf(dateHeure);
                List<Integer> candidates = new ArrayList<>();

                try (PreparedStatement ps =
                        con.prepareStatement(sqlCandidates)) {

                    ps.setInt(1, idRestaurant);
                    ps.setTimestamp(2, timestamp);
                    ps.setTimestamp(3, timestamp);
                    ps.setInt(4, convives);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            candidates.add(
                                rs.getInt("NUM_CRENEAU")
                            );
                        }
                    }
                }

                Integer numCreneau = null;
                String nomRestaurant = null;
                Timestamp debut = null;
                Timestamp fin = null;

                /*
                    On prend le premier créneau réellement
                    encore libre et verrouillable.

                    break arrête la boucle dès qu'un créneau
                    correct est trouvé.
                */

                for (Integer candidate : candidates) {
                    try (PreparedStatement ps =
                            con.prepareStatement(sqlLock)) {

                        ps.setInt(1, candidate);
                        ps.setInt(2, idRestaurant);
                        ps.setInt(3, convives);
                        ps.setTimestamp(4, timestamp);
                        ps.setTimestamp(5, timestamp);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()
                                    && "LIBRE".equals(
                                        rs.getString("STATUT")
                                    )) {

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

                int numReservation = nextReservationId(con);

                try (PreparedStatement ps =
                        con.prepareStatement(sqlInsert)) {

                    ps.setInt(1, numReservation);
                    ps.setInt(2, numCreneau);
                    ps.setString(3, nom);
                    ps.setString(4, prenom);
                    ps.setString(5, telephone);
                    ps.setInt(6, convives);

                    ps.executeUpdate();
                }

                try (PreparedStatement ps =
                        con.prepareStatement(sqlUpdate)) {

                    ps.setInt(1, numCreneau);
                    ps.executeUpdate();
                }

                con.commit();

                return "{"
                    + "\"status\":\"ok\","
                    + "\"message\":\"Réservation confirmée\","
                    + "\"idReservation\":"
                    + numReservation
                    + ",\"restaurant\":"
                    + jsonString(nomRestaurant)
                    + ",\"convives\":"
                    + convives
                    + ",\"debut\":"
                    + jsonString(debut.toString())
                    + ",\"fin\":"
                    + jsonString(fin.toString())
                    + "}";

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println(
                "Erreur reserverTable : " + e.getMessage()
            );

            return jsonError("Réservation impossible");
        }
    }
}
