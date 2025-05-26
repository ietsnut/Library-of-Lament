package window;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.*;
import engine.Manager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwFocusWindow;

public class Window extends JWindow {

    protected int X, Y;
    protected final int W, H;
    protected final ViewBox size;
    protected final SVGDocument svg;
    protected final Canvas2D canvas2D;

    public Window(int W, int H, String svg) {
        this.W = W;
        this.H = H;
        this.size = new ViewBox(W, H);
        SVGLoader loader = new SVGLoader();
        URL url = getClass().getResource("/resources/vector/" + svg + ".svg");
        CustomStrokeProcessor strokeProcessor = new CustomStrokeProcessor();
        this.svg = loader.load(url, new DefaultParserProvider() {
            @Override
            public DomProcessor createPreProcessor() {
                return strokeProcessor;
            }
        });
        setBackground(new Color(0, 0, 0, 0));
        this.canvas2D = new Canvas2D();
        setContentPane(this.canvas2D);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        toFront();
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
                attributes.put("stroke-width", "0.5");
                attributes.put("vector-effect", "non-scaling-stroke");
                attributes.put("stroke", "#43392a");
                attributes.put("fill", "#c7be7d");
            }
        }

    }

    protected class Canvas2D extends JPanel {

        private Canvas2D() {
            super(false);
            setOpaque(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            svg.render((Component) this, g2d, size);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(W, H);
        }

    }

}
