
import perspective.Perspective2D;
import perspective.Perspective3D;

import javax.swing.*;

import static org.lwjgl.glfw.GLFW.*;

public class LibraryOfLament {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new Perspective3D(1000, 1000);
            new Perspective2D(100, 100);
        });
    }
}