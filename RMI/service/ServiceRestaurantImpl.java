import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;


public class ServiceRestaurantImpl extends UnicastRemoteObject implements ServiceRestaurant {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public ServiceRestaurantImpl() throws RemoteException {
        super();
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                                        .getResourceAsStream("config.properties")) {
            if (in == null) throw new RuntimeException("config.properties introuvable");
            props.load(in);
        } catch (Exception e) {
            throw new RemoteException("Erreur chargement config : " + e.getMessage());
        }
        this.dbUrl      = props.getProperty("db.url");
        this.dbUser     = props.getProperty("db.user");
        this.dbPassword = props.getProperty("db.password");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private static String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }

    private static String jsonError(String message) {
        return "{\"status\":\"error\",\"message\":\"" + jsonEscape(message) + "\"}";
    }

    @Override
    public String getRestaurants() throws RemoteException {
        StringBuilder sb = new StringBuilder("[");
        String sql = "SELECT NUM_RESTAURANT, NOM, ADRESSE, LATITUDE, LONGITUDE FROM RESTAURANT ORDER BY NUM_RESTAURANT";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append("{")
                                    .append("\"id\":").append(rs.getInt("NUM_RESTAURANT")).append(",")
                                    .append("\"nom\":\"").append(jsonEscape(rs.getString("NOM"))).append("\",")
                                    .append("\"adresse\":\"").append(jsonEscape(rs.getString("ADRESSE"))).append("\",")
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
            return "{\"status\":\"error\",\"message\":\"Erreur base de données\"}";
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public String reserverTable(String nom, String prenom,
                                int convives, String telephone,
                                int idRestaurant,
                                String date, String heure) throws RemoteException {

        // --- Validation des champs ---
        if (nom == null || nom.isBlank() ||
            prenom == null || prenom.isBlank() ||
            telephone == null || telephone.isBlank() ||
            date == null || date.isBlank() ||
            heure == null || heure.isBlank()) {
            return "{\"status\":\"error\",\"message\":\"Champs obligatoires manquants\"}";
        }
        if (convives <= 0) {
            return "{\"status\":\"error\",\"message\":\"Nombre de convives invalide\"}";
        }

        LocalDate dateValue;
        LocalTime timeValue;
        try {
            dateValue = LocalDate.parse(date, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return jsonError("Format de date invalide, attendu YYYY-MM-DD");
        }

        try {
            timeValue = LocalTime.parse(heure, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return jsonError("Format d'heure invalide, attendu HH:MM");
        }

        LocalDateTime requestedDateTime = LocalDateTime.of(dateValue, timeValue);

        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            String sqlSlot = "SELECT NUM_CRENEAU, NUM_TABLE, DEBUT, FIN, NOM_RESTAURANT, NB_PLACES FROM ("
                           + "    SELECT ct.NUM_CRENEAU, ct.NUM_TABLE, ct.DEBUT, ct.FIN, r.NOM AS NOM_RESTAURANT, tr.NB_PLACES "
                           + "    FROM CRENEAU_TABLE ct "
                           + "    JOIN TABLE_RESTAURANT tr ON tr.NUM_TABLE = ct.NUM_TABLE "
                           + "    JOIN RESTAURANT r ON r.NUM_RESTAURANT = tr.NUM_RESTAURANT "
                           + "    WHERE r.NUM_RESTAURANT = ? "
                           + "      AND ct.STATUT = 'LIBRE' "
                           + "      AND ct.DEBUT <= ? "
                           + "      AND ct.FIN > ? "
                           + "      AND tr.NB_PLACES >= ? "
                           + "    ORDER BY tr.NB_PLACES ASC, ct.DEBUT ASC, ct.NUM_CRENEAU ASC"
                           + ") WHERE ROWNUM = 1 FOR UPDATE";

            int numCreneau;
            String nomResto;
            Timestamp debutCreneau;
            Timestamp finCreneau;

            try (PreparedStatement ps = con.prepareStatement(sqlSlot)) {
                ps.setInt(1, idRestaurant);
                ps.setTimestamp(2, Timestamp.valueOf(requestedDateTime));
                ps.setTimestamp(3, Timestamp.valueOf(requestedDateTime));
                ps.setInt(4, convives);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return jsonError("Aucun créneau disponible pour ce restaurant, cette date et ce nombre de convives");
                    }
                    numCreneau = rs.getInt("NUM_CRENEAU");
                    nomResto = rs.getString("NOM_RESTAURANT");
                    debutCreneau = rs.getTimestamp("DEBUT");
                    finCreneau = rs.getTimestamp("FIN");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE CRENEAU_TABLE SET STATUT = 'RESERVE' WHERE NUM_CRENEAU = ?")) {
                ps.setInt(1, numCreneau);
                ps.executeUpdate();
            }

            String sqlInsert = "INSERT INTO RESERVATION " +
                               "(NUM_RESERVATION, NUM_CRENEAU, NOM_CLIENT, PRENOM_CLIENT, TELEPHONE, NB_CONVIVES) " +
                               "VALUES (SEQ_RESERVATION.NEXTVAL, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setInt(1, numCreneau);
                ps.setString(2, nom);
                ps.setString(3, prenom);
                ps.setString(4, telephone);
                ps.setInt(5, convives);
                ps.executeUpdate();
            }

            con.commit();

            System.out.println("Réservation confirmée — " + prenom + " " + nom
                    + " (" + convives + " pers.) au restaurant " + nomResto
                    + " le " + date + " à " + heure
                    + " sur le créneau " + numCreneau);

            return "{"
                 + "\"status\":\"ok\"," 
                 + "\"message\":\"Réservation confirmée\"," 
                 + "\"restaurant\":\"" + jsonEscape(nomResto) + "\"," 
                 + "\"convives\":" + convives + "," 
                 + "\"date\":\"" + date + "\"," 
                 + "\"heure\":\"" + heure + "\"," 
                 + "\"numeroCreneau\":" + numCreneau + "," 
                 + "\"debut\":\"" + debutCreneau.toLocalDateTime() + "\"," 
                 + "\"fin\":\"" + finCreneau.toLocalDateTime() + "\"}";

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur rollback : " + ex.getMessage());
                }
            }
            System.err.println("Erreur réservation : " + e.getMessage());
            return jsonError("Réservation annulée - conflit ou erreur BD");

        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    System.err.println("Erreur fermeture connexion : " + e.getMessage());
                }
            }
        }
    }
}