package passport;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite database helper for the Passport Automation System.
 * Replaces the old JDBC-ODBC bridge (removed from modern Java).
 */
public class Database {
    private static final String DB_FILE = "data/passport.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;
    private static boolean ready = false;

    public static Connection getConnection() throws SQLException {
        initialize();
        return DriverManager.getConnection(URL);
    }

    public static synchronized void initialize() {
        if (ready) {
            return;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            Files.createDirectories(Paths.get("data"));
            try (Connection con = DriverManager.getConnection(URL);
                 Statement st = con.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
                createTables(st);
                seedUsers(con);
                seedApplications(con);
            }
            ready = true;
        } catch (Exception e) {
            throw new RuntimeException("Could not start the database: " + e.getMessage(), e);
        }
    }

    private static void createTables(Statement st) throws SQLException {
        st.execute("CREATE TABLE IF NOT EXISTS login ("
                + "username TEXT PRIMARY KEY,"
                + "password TEXT NOT NULL,"
                + "role TEXT NOT NULL"
                + ")");

        st.execute("CREATE TABLE IF NOT EXISTS userdetails ("
                + "name TEXT,"
                + "fathername TEXT,"
                + "dateofbirth TEXT,"
                + "gender TEXT,"
                + "permanentaddress TEXT,"
                + "temporaryaddress TEXT,"
                + "phoneno TEXT,"
                + "emailid TEXT,"
                + "proof TEXT PRIMARY KEY,"
                + "adminverify TEXT DEFAULT 'Pending',"
                + "regverify TEXT DEFAULT 'Pending',"
                + "policeverify TEXT DEFAULT 'Pending'"
                + ")");

        st.execute("CREATE TABLE IF NOT EXISTS RegionalDatabase ("
                + "Name TEXT,"
                + "Proof TEXT"
                + ")");

        st.execute("CREATE TABLE IF NOT EXISTS PoliceDb ("
                + "Name TEXT,"
                + "permanentaddress TEXT,"
                + "temporaryaddress TEXT"
                + ")");
    }

    private static void seedUsers(Connection con) throws SQLException {
        if (count(con, "SELECT COUNT(*) FROM login") > 0) {
            return;
        }
        insertUser(con, "admin", "admin123", "admin");
        insertUser(con, "regional", "regional123", "regional");
        insertUser(con, "police", "police123", "police");
        insertUser(con, "applicant", "applicant123", "applicant");
    }

    private static void seedApplications(Connection con) throws SQLException {
        if (count(con, "SELECT COUNT(*) FROM userdetails") > 0) {
            return;
        }

        insertApplication(con, "Rahul Sharma", "Rajesh Sharma", "1998-05-12", "Male",
                "12 MG Road, Chennai", "12 MG Road, Chennai", "9876543210",
                "rahul.sharma@mail.com", "PROOF1001", "Pending", "Pending", "Pending");

        insertApplication(con, "Ananya Iyer", "Suresh Iyer", "1999-08-20", "Female",
                "45 Anna Nagar, Chennai", "8 T Nagar, Chennai", "9123456780",
                "ananya.iyer@mail.com", "PROOF1002", "Verified", "Pending", "Pending");
        insertRegional(con, "Ananya Iyer", "PROOF1002");

        insertApplication(con, "Vikram Patel", "Mahesh Patel", "1995-01-03", "Male",
                "22 Ring Road, Coimbatore", "22 Ring Road, Coimbatore", "9988776655",
                "vikram.patel@mail.com", "PROOF1003", "Verified", "Verified", "Pending");
        insertRegional(con, "Vikram Patel", "PROOF1003");
        insertPolice(con, "Vikram Patel", "22 Ring Road, Coimbatore", "22 Ring Road, Coimbatore");
    }

    private static void insertUser(Connection con, String username, String password, String role)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO login(username, password, role) VALUES (?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    private static void insertApplication(Connection con, String name, String father, String dob,
            String gender, String permanent, String temporary, String phone, String email,
            String proof, String admin, String regional, String police) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO userdetails(name, fathername, dateofbirth, gender, permanentaddress, "
                        + "temporaryaddress, phoneno, emailid, proof, adminverify, regverify, policeverify) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, father);
            ps.setString(3, dob);
            ps.setString(4, gender);
            ps.setString(5, permanent);
            ps.setString(6, temporary);
            ps.setString(7, phone);
            ps.setString(8, email);
            ps.setString(9, proof);
            ps.setString(10, admin);
            ps.setString(11, regional);
            ps.setString(12, police);
            ps.executeUpdate();
        }
    }

    public static void insertRegional(Connection con, String name, String proof) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO RegionalDatabase(Name, Proof) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, proof);
            ps.executeUpdate();
        }
    }

    public static void insertPolice(Connection con, String name, String permanent, String temporary)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO PoliceDb(Name, permanentaddress, temporaryaddress) VALUES (?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, permanent);
            ps.setString(3, temporary);
            ps.executeUpdate();
        }
    }

    public static String[] authenticate(String username, String password) {
        String sql = "SELECT username, role FROM login WHERE username = ? AND password = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[] { rs.getString("username"), rs.getString("role") };
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static int count(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
