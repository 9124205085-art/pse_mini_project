package passport;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts the Passport Automation System. */
public class Main {
    public static void main(String[] args) {
        launch();
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep the default look if the system theme is unavailable.
        }
        Database.initialize();
        SwingUtilities.invokeLater(LoginDemo::new);
    }
}
