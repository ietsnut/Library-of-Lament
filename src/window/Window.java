package window;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Window extends JFrame implements Runnable, MouseListener, MouseMotionListener, MouseWheelListener {

    protected int X, Y;
    protected final int W, H;
    protected final int FPS = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDisplayMode().getRefreshRate();
    protected final Canvas2D canvas2D;
    //protected final BufferedImage image;

    protected final SVGDocument svg;

    public Window(int W, int H, String svg) {
        this.W = W;
        this.H = H;
        //this.image = image;
        SVGLoader loader = new SVGLoader();
        //URL url = getClass().getResource("/resources/vector/" + svg + ".svg");
        CustomStrokeProcessor strokeProcessor = new CustomStrokeProcessor();
        String svgString = "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'>"
                + "<circle cx='100' cy='100' r='80' fill='blue' stroke='black' stroke-width='1px'/>"
                + "</svg>";
        InputStream svgStream = new ByteArrayInputStream(svgString.getBytes(StandardCharsets.UTF_8));

        this.svg = loader.load(svgStream, new DefaultParserProvider() {
            @Override
            public DomProcessor createPreProcessor() {
                return strokeProcessor;
            }
        });
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.uiScale", "1");
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        addMouseListener(this);
        addMouseMotionListener(this);
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

    public class CustomStrokeProcessor implements DomProcessor {

        @Override
        public void process(ParsedElement root) {
            processElement(root);
            for (ParsedElement child : root.children()) {
                process(child);
            }
        }

        private void processElement(ParsedElement element) {
            Map<String, String> attributes = element.attributeNode().attributes();
            if (attributes.containsKey("stroke")) {
                // Force the stroke width to a fixed value (1px)
                attributes.put("stroke-width", "1px");
                // Optionally, try to force non-scaling stroke behavior if supported.
                attributes.put("vector-effect", "non-scaling-stroke");
            }
        }

    }

    public void update() {

    }

    public void draw(Graphics2D g) {
        svg.render((JComponent) canvas2D, g);
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
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);

            //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            window.draw(g2d);
            //svg.render(this, g2d, new ViewBox(0, 0, W, H));
             //g2d.drawImage(window.image, 0, 0, W, H, this);
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
    public void mouseWheelMoved(MouseWheelEvent e) {

    }


}
