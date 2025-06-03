package window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public abstract class SwingWindow extends JFrame implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {

    protected int X, Y;
    protected final int W, H;
    protected HashMap<Integer, Boolean> KEYS;
    protected final int FPS = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDisplayMode().getRefreshRate();
    protected final Canvas2D canvas2D;
    protected final BufferedImage border;

    public SwingWindow(BufferedImage border, int W, int H) {
        this.W = W;
        this.H = H;
        this.border = border;
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
    }

    public abstract void draw(Graphics2D graphics);

    protected class Canvas2D extends JPanel {

        private final SwingWindow swingWindow;

        private Canvas2D(SwingWindow swingWindow) {
            super(true);
            this.swingWindow = swingWindow;
            setOpaque(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g;
            swingWindow.draw(g2d);
            g2d.drawImage(swingWindow.border, 0, 0, W, H, this);
        }

        @Override
        public Dimension getPreferredSize() {
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
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            int x2 = e.getX() - X;
            int y2 = e.getY() - Y;
            setLocation(getLocation().x + x2, getLocation().y + y2);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        KEYS.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        KEYS.put(e.getKeyCode(), false);
    }

}
