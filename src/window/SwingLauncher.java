package window;

import javax.swing.*;

import java.awt.*;

import static resource.Material.*;

public class SwingLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Window window = new Window(indexs(load("/resources/rat_top.png")), 1024, 1366);
        });
    }
}
