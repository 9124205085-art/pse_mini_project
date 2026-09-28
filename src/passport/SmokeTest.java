package passport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Headless check of login and the three-step verification flow. */
public class SmokeTest {
    public static void main(String[] args) throws Exception {
        Database.initialize();

        check(Database.authenticate("admin", "admin123") != null, "admin login");
        check(Database.authenticate("regional", "wrong") == null, "bad password rejected");

        String proof = "SMOKE" + System.currentTimeMillis();
        try (Connection con = Database.getConnection();
             PreparedStatement insert = con.prepareStatement(
                     "INSERT INTO userdetails(name, fathername, dateofbirth, gender, permanentaddress, "
                             + "temporaryaddress, phoneno, emailid, proof) "
                             + "VALUES ('Smoke User', 'Parent', '2000-01-01', 'Other', "
                             + "'1 Test Street', '1 Test Street', '9000000000', 'smoke@mail.com', ?)")) {
            insert.setString(1, proof);
            insert.executeUpdate();
        }

        try (Connection con = Database.getConnection();
             PreparedStatement admin = con.prepareStatement(
                     "UPDATE userdetails SET adminverify = 'Verified' WHERE proof = ?")) {
            admin.setString(1, proof);
            admin.executeUpdate();
            Database.insertRegional(con, "Smoke User", proof);
        }

        try (Connection con = Database.getConnection();
             PreparedStatement regional = con.prepareStatement(
                     "UPDATE userdetails SET regverify = 'Verified' WHERE proof = ?")) {
            regional.setString(1, proof);
            regional.executeUpdate();
            Database.insertPolice(con, "Smoke User", "1 Test Street", "1 Test Street");
        }

        try (Connection con = Database.getConnection();
             PreparedStatement police = con.prepareStatement(
                     "UPDATE userdetails SET policeverify = 'Verified' WHERE proof = ?");
             PreparedStatement lookup = con.prepareStatement(
                     "SELECT adminverify, regverify, policeverify FROM userdetails WHERE proof = ?")) {
            police.setString(1, proof);
            police.executeUpdate();
            lookup.setString(1, proof);
            try (ResultSet rs = lookup.executeQuery()) {
                check(rs.next(), "smoke application exists");
                check("Verified".equals(rs.getString(1))
                        && "Verified".equals(rs.getString(2))
                        && "Verified".equals(rs.getString(3)), "all three desks verified");
            }
        }

        System.out.println("SMOKE TEST PASSED");
    }

    private static void check(boolean ok, String label) {
        if (!ok) {
            throw new IllegalStateException("Failed: " + label);
        }
        System.out.println("OK  " + label);
    }
}
