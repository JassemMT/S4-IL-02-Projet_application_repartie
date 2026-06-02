import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.Properties;

public class ServiceRestaurantImpl extends UnicastRemoteObject implements ServiceRestaurant {

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

    @Override
    public String getRestaurants() throws RemoteException {
        StringBuilder sb = new StringBuilder("[");
        String sql = "SELECT id, nom, adresse, description, lat, lng FROM RESTAURANTS";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append("{")
                  .append("\"id\":").append(rs.getInt("id")).append(",")
                  .append("\"nom\":\"").append(rs.getString("nom")).append("\",")
                  .append("\"adresse\":\"").append(rs.getString("adresse")).append("\",")
                  .append("\"description\":\"").append(rs.getString("description")).append("\",")
                  .append("\"lat\":").append(rs.getDouble("lat")).append(",")
                  .append("\"lng\":").append(rs.getDouble("lng"))
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

        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false); // début de transaction

            // --- Vérification que le restaurant existe ---
            // SELECT FOR UPDATE : Oracle pose un verrou sur la ligne
            // Si deux clients arrivent en même temps sur le même restaurant,
            // le second attend que le premier ait commité ou rollbacké
            String sqlLock = "SELECT nom FROM RESTAURANTS WHERE id = ? FOR UPDATE";
            String nomResto = null;

            try (PreparedStatement ps = con.prepareStatement(sqlLock)) {
                ps.setInt(1, idRestaurant);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return "{\"status\":\"error\",\"message\":\"Restaurant introuvable\"}";
                    }
                    nomResto = rs.getString("nom");
                }
            }

            // --- INSERT de la réservation ---
            String sqlInsert = "INSERT INTO RESERVATIONS " +
                               "(id, id_restaurant, nom, prenom, nb_convives, telephone, date_heure_resa) " +
                               "VALUES (SEQ_RESERVATIONS.NEXTVAL, ?, ?, ?, ?, ?, " +
                               "TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI'))";

            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setInt(1, idRestaurant);
                ps.setString(2, nom);
                ps.setString(3, prenom);
                ps.setInt(4, convives);
                ps.setString(5, telephone);
                ps.setString(6, date + " " + heure);
                ps.executeUpdate();
            }

            con.commit(); // validation — verrou libéré ici

            System.out.println("Réservation confirmée — " + prenom + " " + nom
                    + " (" + convives + " pers.) au restaurant " + nomResto
                    + " le " + date + " à " + heure);

            return "{\"status\":\"ok\","
                 + "\"message\":\"Réservation confirmée\","
                 + "\"restaurant\":\"" + nomResto + "\","
                 + "\"convives\":" + convives + ","
                 + "\"date\":\"" + date + "\","
                 + "\"heure\":\"" + heure + "\"}";

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // annulation si conflit ou erreur
                } catch (SQLException ex) {
                    System.err.println("Erreur rollback : " + ex.getMessage());
                }
            }
            System.err.println("Erreur réservation : " + e.getMessage());
            return "{\"status\":\"error\",\"message\":\"Réservation annulée — conflit ou erreur BD\"}";

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