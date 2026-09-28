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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** Applicant data-entry form. Saves a new passport application. */
public class CreateAccount extends JFrame {
    private final JTextField nameField = UITheme.field();
    private final JTextField fatherField = UITheme.field();
    private final JTextField dobField = UITheme.field();
    private final JComboBox<String> genderBox = new JComboBox<>(
            new String[] {"Male", "Female", "Other"});
    private final JTextField permanentField = UITheme.field();
    private final JTextField temporaryField = UITheme.field();
    private final JTextField proofField = UITheme.field();
    private final JTextField phoneField = UITheme.field();
    private final JTextField emailField = UITheme.field();

    public CreateAccount() {
        setTitle("Passport Automation System: Data Entry");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 720);
        setMinimumSize(new Dimension(760, 640));
        getContentPane().setBackground(UITheme.PAGE);
        setLayout(new BorderLayout());

        add(UITheme.header("Applicant Data Entry", e -> logout()), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        UITheme.center(this);
        setVisible(true);
    }

    private JScrollPane buildForm() {
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addRow(card, gbc, 0, "Name", nameField);
        addRow(card, gbc, 1, "Father name", fatherField);
        addRow(card, gbc, 2, "D.O.B (YYYY-MM-DD)", dobField);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        card.add(UITheme.label("Gender"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        genderBox.setFont(UITheme.BODY);
        card.add(genderBox, gbc);

        addRow(card, gbc, 4, "Permanent address", permanentField);
        addRow(card, gbc, 5, "Temporary address", temporaryField);
        addRow(card, gbc, 6, "Proof number", proofField);
        addRow(card, gbc, 7, "Contact number", phoneField);
        addRow(card, gbc, 8, "Email id", emailField);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton save = UITheme.button("Save", UITheme.SUCCESS);
        JButton status = UITheme.button("Check Status", UITheme.PRIMARY);
        JButton reset = UITheme.button("Reset", UITheme.GOLD);
        save.addActionListener(e -> saveApplication());
        status.addActionListener(e -> checkStatus());
        reset.addActionListener(e -> resetFields());
        actions.add(save);
        actions.add(status);
        actions.add(reset);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        card.add(actions, gbc);

        gbc.gridy = 10;
        card.add(UITheme.muted("Proof number is the applicant ID used by Admin, Regional, and Police desks."), gbc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.PAGE);
        wrap.setBorder(new EmptyBorder(20, 24, 20, 24));
        wrap.add(card, BorderLayout.NORTH);
        return new JScrollPane(wrap);
    }

    private void addRow(JPanel card, GridBagConstraints gbc, int row, String caption, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(UITheme.label(caption), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(field, gbc);
    }

    private void saveApplication() {
        String name = nameField.getText().trim();
        String father = fatherField.getText().trim();
        String dob = dobField.getText().trim();
        String gender = String.valueOf(genderBox.getSelectedItem());
        String permanent = permanentField.getText().trim();
        String temporary = temporaryField.getText().trim();
        String proof = proofField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || father.isEmpty() || dob.isEmpty() || permanent.isEmpty()
                || temporary.isEmpty() || proof.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill every field.",
                    "Missing data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Contact number must be 10 digits.",
                    "Invalid phone", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email id.",
                    "Invalid email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO userdetails(name, fathername, dateofbirth, gender, permanentaddress, "
                + "temporaryaddress, phoneno, emailid, proof, adminverify, regverify, policeverify) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending', 'Pending', 'Pending')";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, father);
            ps.setString(3, dob);
            ps.setString(4, gender);
            ps.setString(5, permanent);
            ps.setString(6, temporary);
            ps.setString(7, phone);
            ps.setString(8, email);
            ps.setString(9, proof);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Application saved. Proof number: " + proof + "\nIt now waits for Admin verification.",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save. Proof number may already exist.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkStatus() {
        String proof = proofField.getText().trim();
        if (proof.isEmpty()) {
            proof = JOptionPane.showInputDialog(this, "Enter proof number to check status:");
            if (proof == null || proof.trim().isEmpty()) {
                return;
            }
            proof = proof.trim();
        }

        String sql = "SELECT name, adminverify, regverify, policeverify FROM userdetails WHERE proof = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, proof);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "No application found for " + proof,
                            "Not found", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String admin = rs.getString("adminverify");
                String regional = rs.getString("regverify");
                String police = rs.getString("policeverify");
                String ready = "Verified".equals(admin) && "Verified".equals(regional)
                        && "Verified".equals(police) ? "Ready for passport issue" : "In process";
                JOptionPane.showMessageDialog(this,
                        "Applicant: " + rs.getString("name")
                                + "\nAdmin: " + admin
                                + "\nRegional: " + regional
                                + "\nPolice: " + police
                                + "\nOverall: " + ready,
                        "Application status", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFields() {
        nameField.setText("");
        fatherField.setText("");
        dobField.setText("");
        genderBox.setSelectedIndex(0);
        permanentField.setText("");
        temporaryField.setText("");
        proofField.setText("");
        phoneField.setText("");
        emailField.setText("");
    }

    private void logout() {
        dispose();
        new LoginDemo();
    }

    public static void main(String[] args) {
        Main.launch();
    }
}
