package passport;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** Shared colors and Swing helpers so every screen looks consistent. */
public final class UITheme {
    public static final Color NAVY = new Color(13, 40, 71);
    public static final Color NAVY_DARK = new Color(8, 26, 48);
    public static final Color GOLD = new Color(196, 154, 71);
    public static final Color PAGE = new Color(236, 241, 247);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(30, 41, 59);
    public static final Color MUTED = new Color(71, 85, 105);
    public static final Color SUCCESS = new Color(22, 101, 52);
    public static final Color DANGER = new Color(153, 27, 27);
    public static final Color PRIMARY = new Color(29, 78, 137);

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private UITheme() {
    }

    public static JPanel header(String title, ActionListener logoutAction) {
        JPanel bar = new JPanel();
        bar.setBackground(NAVY);
        bar.setPreferredSize(new Dimension(0, 72));
        bar.setLayout(new java.awt.BorderLayout());
        bar.setBorder(new EmptyBorder(0, 20, 0, 16));

        JLabel label = new JLabel("Passport Automation System  |  " + title);
        label.setForeground(Color.WHITE);
        label.setFont(TITLE);
        bar.add(label, java.awt.BorderLayout.WEST);

        if (logoutAction != null) {
            JButton logout = button("Logout", DANGER);
            logout.addActionListener(logoutAction);
            bar.add(logout, java.awt.BorderLayout.EAST);
        }
        return bar;
    }

    public static JButton button(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    public static JTextField field() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        styleField(field);
        return field;
    }

    public static void styleField(JTextField field) {
        field.setFont(BODY);
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(6, 8, 6, 8)));
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    public static void center(Window frame) {
        frame.setLocationRelativeTo(null);
    }
}
