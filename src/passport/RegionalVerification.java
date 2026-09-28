package passport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** Regional officer desk: find an admin-approved file and mark regional verification. */
public class RegionalVerification extends JFrame {
    private final JTextField nameField = UITheme.field();
    private final JTextField proofField = UITheme.field();

    public RegionalVerification() {
        setTitle("Regional Officer Verification");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 520);
        setMinimumSize(new Dimension(720, 460));
        getContentPane().setBackground(UITheme.PAGE);
        setLayout(new BorderLayout());

        add(UITheme.header("Regional Officer Verification", e -> logout()), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        UITheme.center(this);
        setVisible(true);
    }

    private JPanel buildForm() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UITheme.PAGE);
        wrap.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(520, 280));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(UITheme.label("Find an admin-verified applicant by name and proof number."), gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        card.add(UITheme.label("Name"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        card.add(UITheme.label("Proof no"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(proofField, gbc);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton find = UITheme.button("Find", UITheme.PRIMARY);
        JButton verify = UITheme.button("Verified", UITheme.SUCCESS);
        JButton police = UITheme.button("Police Verification", UITheme.GOLD);
        find.addActionListener(e -> findApplicant(false));
        verify.addActionListener(e -> findApplicant(true));
        police.addActionListener(e -> openPolice());
        actions.add(find);
        actions.add(verify);
        actions.add(police);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        card.add(actions, gbc);

        gbc.gridy = 4;
        card.add(UITheme.muted("Sample ready for this desk: Ananya Iyer / PROOF1002"), gbc);

        wrap.add(card);
        return wrap;
    }

    private void findApplicant(boolean alsoVerify) {
        String name = nameField.getText().trim();
        String proof = proofField.getText().trim();
        if (name.isEmpty() || proof.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type the name and proof number.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT Name, Proof FROM RegionalDatabase WHERE Name = ? AND Proof = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, proof);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "Details not found. Admin must verify the file first.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "User detail found.",
                    "Regional desk", JOptionPane.INFORMATION_MESSAGE);
            if (alsoVerify) {
                markVerified(con, name, proof);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markVerified(Connection con, String name, String proof) throws Exception {
        try (PreparedStatement update = con.prepareStatement(
                "UPDATE userdetails SET regverify = 'Verified' WHERE name = ? AND proof = ?");
             PreparedStatement lookup = con.prepareStatement(
                     "SELECT permanentaddress, temporaryaddress FROM userdetails WHERE proof = ?")) {
            update.setString(1, name);
            update.setString(2, proof);
            update.executeUpdate();

            lookup.setString(1, proof);
            try (ResultSet rs = lookup.executeQuery()) {
                if (rs.next() && !alreadyInPolice(con, name, rs.getString(1), rs.getString(2))) {
                    Database.insertPolice(con, name, rs.getString(1), rs.getString(2));
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Regional verification completed. File sent to Police.",
                "Verified", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean alreadyInPolice(Connection con, String name, String permanent, String temporary)
            throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM PoliceDb WHERE Name = ? AND permanentaddress = ? AND temporaryaddress = ?")) {
            ps.setString(1, name);
            ps.setString(2, permanent);
            ps.setString(3, temporary);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void openPolice() {
        dispose();
        new PoliceVerification();
    }

    private void logout() {
        dispose();
        new LoginDemo();
    }

    public static void main(String[] args) {
        Main.launch();
    }
}
