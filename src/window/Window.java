package window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class Window extends JFrame implements Runnable, MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {

    protected int X, Y;
    protected final int W, H;
    protected HashMap<Integer, Boolean> KEYS;
    protected final int FPS = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDisplayMode().getRefreshRate();
    protected final Canvas2D canvas2D;
    protected final BufferedImage image;

    public Window(BufferedImage image, int W, int H) {
        this.W = W;
        this.H = H;
        this.image = image;
        this.KEYS = new HashMap<>();
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                try {
                    KEYS.put(field.getInt(null), false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.uiScale", "1");
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addMouseWheelListener(this);
        setFocusable(true);
        setFocusableWindowState(true);
        this.canvas2D = new Canvas2D(this);
        setContentPane(this.canvas2D);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        transferFocus();
        setAlwaysOnTop(true);
        SwingUtilities.invokeLater(this);
    }

    public void update() {

    }

    public void draw(Graphics2D graphics) {

    }

    long previousTime = System.nanoTime();
    long deltaTime = 0;
    final long timePerFrame = 1000000000 / FPS;

    @Override
    public void run() {
        long currentTime = System.nanoTime();
        deltaTime = currentTime - previousTime;
        previousTime = currentTime;
        while (deltaTime > timePerFrame) {
            deltaTime -= timePerFrame;
            update();
        }
        canvas2D.repaint();
        SwingUtilities.invokeLater(this);
    }

    protected class Canvas2D extends JPanel {

        private final Window window;

        private Canvas2D(Window window) {
            super(true);
            this.window = window;
            setOpaque(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g;
            window.draw(g2d);
            g2d.drawImage(window.image, 0, 0, W, H, this);
        }

        @Override
        public Dimension getPreferredSize() {
            //return window.image == null ? new Dimension(W, H) : new Dimension(window.image.getWidth(), window.image.getHeight());
            return new Dimension(W, H);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            X = e.getX();
            Y = e.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            X = e.getX();
            Y = e.getY();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            int x2 = e.getX() - X;
            int y2 = e.getY() - Y;
            setLocation(getLocation().x + x2, getLocation().y + y2);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        KEYS.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        KEYS.put(e.getKeyCode(), false);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }


}
