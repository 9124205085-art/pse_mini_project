package passport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Login screen. Routes each role to the matching workstation. */
public class LoginDemo extends JFrame {
    private final JTextField usernameField = UITheme.field();
    private final JPasswordField passwordField = UITheme.passwordField();
    private JButton loginButton;

    public LoginDemo() {
        setTitle("Passport Automation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 620);
        setMinimumSize(new Dimension(860, 540));
        getContentPane().setBackground(UITheme.PAGE);
        setLayout(new BorderLayout());

        add(buildBanner(), BorderLayout.NORTH);
        add(buildCard(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        UITheme.center(this);
        setVisible(true);
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel buildBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(UITheme.NAVY);
        banner.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel kicker = new JLabel("GOVERNMENT OF INDIA  ·  MINI PROJECT");
        kicker.setForeground(UITheme.GOLD);
        kicker.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel title = new JLabel("Passport Automation System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel subtitle = new JLabel("Applicant entry  ·  Admin check  ·  Regional office  ·  Police verification");
        subtitle.setForeground(new Color(203, 213, 225));
        subtitle.setFont(UITheme.BODY);

        JPanel text = new JPanel(new GridBagLayout());
        text.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        text.add(kicker, gbc);
        gbc.gridy = 1;
        text.add(title, gbc);
        gbc.gridy = 2;
        text.add(subtitle, gbc);
        banner.add(text, BorderLayout.WEST);
        return banner;
    }

    private JPanel buildCard() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UITheme.PAGE);

        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(460, 340));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 16, 0);

        JLabel heading = new JLabel("Secure Login");
        heading.setFont(UITheme.HEADING);
        heading.setForeground(UITheme.NAVY);
        card.add(heading, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 0, 4, 12);
        card.add(UITheme.label("Username"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        usernameField.setColumns(18);
        card.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        card.add(UITheme.label("Password"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        card.add(passwordField, gbc);

        loginButton = UITheme.button("Login", UITheme.PRIMARY);
        loginButton.addActionListener(e -> attemptLogin());

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 0, 8, 0);
        card.add(loginButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 0, 0, 0);
        card.add(UITheme.muted("<html>Demo logins — admin/admin123 · regional/regional123<br>"
                + "police/police123 · applicant/applicant123</html>"), gbc);

        wrap.add(card);
        return wrap;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(UITheme.NAVY_DARK);
        JLabel note = new JLabel("Applications move through Admin → Regional Officer → Police before issue.");
        note.setForeground(new Color(203, 213, 225));
        note.setFont(UITheme.SMALL);
        footer.add(note);
        return footer;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.",
                    "Login required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String[] user = Database.authenticate(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Incorrect login or password.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            openWorkspace(user[1]);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openWorkspace(String role) {
        dispose();
        switch (role) {
            case "admin":
                SwingUtilities.invokeLater(AdminVerification::new);
                break;
            case "regional":
                SwingUtilities.invokeLater(RegionalVerification::new);
                break;
            case "police":
                SwingUtilities.invokeLater(PoliceVerification::new);
                break;
            default:
                SwingUtilities.invokeLater(CreateAccount::new);
                break;
        }
    }

    public static void main(String[] args) {
        Main.launch();
    }
}
