package passport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Admin desk: search, delete, and mark admin verification. */
public class AdminVerification extends JFrame {
    private final JComboBox<String> searchField = new JComboBox<>(
            new String[] {"proof", "name", "phoneno", "emailid"});
    private final JTextField searchText = UITheme.field();
    private final JLabel message = UITheme.muted("Search an application or load all records.");
    private final DefaultTableModel model = new DefaultTableModel(new String[] {
            "name", "fathername", "dateofbirth", "gender", "permanentaddress",
            "temporaryaddress", "phoneno", "emailid", "proof",
            "adminverify", "regverify", "policeverify"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public AdminVerification() {
        setTitle("Passport Administrator Verification");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(960, 600));
        getContentPane().setBackground(UITheme.PAGE);
        setLayout(new BorderLayout());

        add(UITheme.header("Admin Verification", e -> logout()), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        UITheme.center(this);
        setVisible(true);
        loadAll();
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBackground(UITheme.PAGE);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel tools = UITheme.card();
        tools.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        tools.add(UITheme.label("Search by"));
        searchField.setFont(UITheme.BODY);
        tools.add(searchField);
        searchText.setColumns(16);
        tools.add(searchText);

        JButton search = UITheme.button("Search", UITheme.PRIMARY);
        JButton load = UITheme.button("Load All", UITheme.NAVY);
        JButton verify = UITheme.button("Verified", UITheme.SUCCESS);
        JButton delete = UITheme.button("Delete", UITheme.DANGER);
        JButton regional = UITheme.button("Regional Verification", UITheme.GOLD);

        search.addActionListener(e -> searchRecords());
        load.addActionListener(e -> loadAll());
        verify.addActionListener(e -> markVerified());
        delete.addActionListener(e -> deleteRecord());
        regional.addActionListener(e -> openRegional());

        tools.add(search);
        tools.add(load);
        tools.add(verify);
        tools.add(delete);
        tools.add(regional);

        table.setFont(UITheme.SMALL);
        table.setRowHeight(24);
        table.getTableHeader().setFont(UITheme.SMALL);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(i == 4 || i == 5 ? 160 : 110);
        }

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(message, BorderLayout.WEST);

        body.add(tools, BorderLayout.NORTH);
        body.add(new JScrollPane(table), BorderLayout.CENTER);
        body.add(south, BorderLayout.SOUTH);
        return body;
    }

    private void loadAll() {
        fillTable("SELECT * FROM userdetails ORDER BY name", null);
    }

    private void searchRecords() {
        String column = String.valueOf(searchField.getSelectedItem());
        String value = searchText.getText().trim();
        if (value.isEmpty()) {
            message.setText("Please type a search value.");
            return;
        }
        fillTable("SELECT * FROM userdetails WHERE " + column + " = ?", value);
    }

    private void fillTable(String sql, String value) {
        model.setRowCount(0);
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (value != null) {
                ps.setString(1, value);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                int rows = 0;
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 0; i < cols; i++) {
                        row[i] = rs.getString(i + 1);
                    }
                    model.addRow(row);
                    rows++;
                }
                message.setText(rows == 0
                        ? "Details not available. Try another search."
                        : rows + " record(s) found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database problem: " + e.getMessage(),
                    "Passport System", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void markVerified() {
        String proof = selectedProof();
        if (proof == null) {
            proof = JOptionPane.showInputDialog(this, "Enter proof number to verify:");
        }
        if (proof == null || proof.trim().isEmpty()) {
            return;
        }
        proof = proof.trim();

        String find = "SELECT name FROM userdetails WHERE proof = ?";
        String update = "UPDATE userdetails SET adminverify = 'Verified' WHERE proof = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement lookup = con.prepareStatement(find);
             PreparedStatement ps = con.prepareStatement(update)) {
            lookup.setString(1, proof);
            String name;
            try (ResultSet rs = lookup.executeQuery()) {
                if (!rs.next()) {
                    message.setText("No application found for proof " + proof);
                    return;
                }
                name = rs.getString("name");
            }
            ps.setString(1, proof);
            ps.executeUpdate();
            if (!alreadyListed(con, "SELECT COUNT(*) FROM RegionalDatabase WHERE Proof = ?", proof)) {
                Database.insertRegional(con, name, proof);
            }
            message.setText("User detail verified and sent to Regional Officer.");
            loadAll();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRecord() {
        String proof = selectedProof();
        if (proof == null) {
            proof = JOptionPane.showInputDialog(this, "Enter the proof number to delete:");
        }
        if (proof == null || proof.trim().isEmpty()) {
            return;
        }
        proof = proof.trim();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete application " + proof + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM userdetails WHERE proof = ?")) {
            ps.setString(1, proof);
            int n = ps.executeUpdate();
            message.setText(n > 0 ? "User detail deleted." : "No matching record.");
            loadAll();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String selectedProof() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        Object value = model.getValueAt(row, 8);
        return value == null ? null : value.toString();
    }

    private boolean alreadyListed(Connection con, String sql, String value) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void openRegional() {
        dispose();
        new RegionalVerification();
    }

    private void logout() {
        dispose();
        new LoginDemo();
    }

    public static void main(String[] args) {
        Main.launch();
    }
}
