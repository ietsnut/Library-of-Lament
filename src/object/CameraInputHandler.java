package object;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class CameraInputHandler implements KeyListener, MouseMotionListener, MouseListener {

    private static final Set<Integer> keysPressed = new HashSet<>();
    public static int dx = 0, dy = 0;
    private static int lastX = -1, lastY = -1;
    public static boolean mouseCaptured = true;

    Canvas canvas;

    public CameraInputHandler(Canvas canvas) {
        this.canvas = canvas;
    }

    public static boolean isKeyDown(int keyCode) {
        return keysPressed.contains(keyCode);
    }

    public static void resetMouseDelta() {
        dx = 0;
        dy = 0;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keysPressed.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysPressed.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private boolean ignoreNextMove = false;

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!mouseCaptured) return;

        if (ignoreNextMove) {
            ignoreNextMove = false;
            return;
        }

        int centerX = canvas.getWidth() / 2;
        int centerY = canvas.getHeight() / 2;
        int mouseX = e.getX();
        int mouseY = e.getY();

        dx += mouseX - centerX;
        dy += mouseY - centerY;

        try {
            Robot robot = new Robot();
            Point p = new Point(centerX, centerY);
            SwingUtilities.convertPointToScreen(p, canvas);
            ignoreNextMove = true;
            robot.mouseMove(p.x, p.y);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Optional: mouse click logic
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {}
}
