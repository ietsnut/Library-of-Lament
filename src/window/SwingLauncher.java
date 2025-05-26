package window;

import javax.swing.*;

public class SwingLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Window window = new Window(1000, 1000, "1");
        });
    }
}
