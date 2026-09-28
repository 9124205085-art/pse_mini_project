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

/** Police desk: confirm address details and complete the last verification. */
public class PoliceVerification extends JFrame {
    private final JTextField nameField = UITheme.field();
    private final JTextField permanentField = UITheme.field();
    private final JTextField temporaryField = UITheme.field();

    public PoliceVerification() {
        setTitle("Passport Automation System: Police Verification");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 560);
        setMinimumSize(new Dimension(740, 500));
        getContentPane().setBackground(UITheme.PAGE);
        setLayout(new BorderLayout());

        add(UITheme.header("Police Verification", e -> logout()), BorderLayout.NORTH);
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
        card.setPreferredSize(new Dimension(560, 320));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(UITheme.label("Confirm name and both addresses for a regionally verified file."), gbc);

        addRow(card, gbc, 1, "Name", nameField);
        addRow(card, gbc, 2, "Permanent address", permanentField);
        addRow(card, gbc, 3, "Temporary address", temporaryField);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton find = UITheme.button("Find", UITheme.PRIMARY);
        JButton verify = UITheme.button("Verified", UITheme.SUCCESS);
        find.addActionListener(e -> findApplicant(false));
        verify.addActionListener(e -> findApplicant(true));
        actions.add(find);
        actions.add(verify);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        card.add(actions, gbc);

        gbc.gridy = 5;
        card.add(UITheme.muted("<html>Sample ready for this desk: Vikram Patel<br>"
                + "22 Ring Road, Coimbatore / 22 Ring Road, Coimbatore</html>"), gbc);

        wrap.add(card);
        return wrap;
    }

    private void addRow(JPanel card, GridBagConstraints gbc, int row, String caption, JTextField field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        card.add(UITheme.label(caption), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(field, gbc);
    }

    private void findApplicant(boolean alsoVerify) {
        String name = nameField.getText().trim();
        String permanent = permanentField.getText().trim();
        String temporary = temporaryField.getText().trim();
        if (name.isEmpty() || permanent.isEmpty() || temporary.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type the name and both addresses.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT Name FROM PoliceDb WHERE Name = ? AND permanentaddress = ? AND temporaryaddress = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, permanent);
            ps.setString(3, temporary);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "Details not found. Regional officer must verify the file first.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "User detail found.",
                    "Police desk", JOptionPane.INFORMATION_MESSAGE);
            if (alsoVerify) {
                try (PreparedStatement update = con.prepareStatement(
                        "UPDATE userdetails SET policeverify = 'Verified' "
                                + "WHERE name = ? AND permanentaddress = ? AND temporaryaddress = ?")) {
                    update.setString(1, name);
                    update.setString(2, permanent);
                    update.setString(3, temporary);
                    update.executeUpdate();
                }
                JOptionPane.showMessageDialog(this,
                        "Police verification completed. Passport is ready for issue.",
                        "Verified", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        dispose();
        new LoginDemo();
    }

    public static void main(String[] args) {
        Main.launch();
    }
}
