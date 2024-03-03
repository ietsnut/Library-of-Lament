package perspective;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.glfw.*;
import org.lwjgl.system.*;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL40.*;

public class Perspective3D extends Perspective2D {

    protected final Canvas3D canvas3D;
    protected final JFrame perspective3D;

    public Perspective3D(int W, int H) {
        super(W, H);
        this.perspective3D = new JFrame();
        this.perspective3D.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.perspective3D.setLayout(new BorderLayout());
        this.perspective3D.setPreferredSize(new Dimension(W - 200, H - 200));
        GLData data = new GLData();
        this.canvas3D = new Canvas3D(data);
        this.perspective3D.add(canvas3D);
        this.perspective3D.setUndecorated(true);
        this.perspective3D.pack();
        this.perspective3D.setLocationRelativeTo(null);
        this.perspective3D.setVisible(true);
        Runnable renderLoop = new Runnable() {
            @Override
            public void run() {
                if (!canvas3D.isValid()) {
                    GL.setCapabilities(null);
                    return;
                }
                canvas3D.render();
                perspective3D.setLocation(getLocation().x + 100, getLocation().y + 100);
                toFront();
                SwingUtilities.invokeLater(this);
            }
        };
        SwingUtilities.invokeLater(renderLoop);
        transferFocus();
        toFront();
    }

    @Override
    public void update() {
        super.update();

    }

    @Override
    public void draw(Graphics2D graphics) {
        graphics.setColor(Color.RED);
        graphics.setStroke(new BasicStroke(100));
        graphics.drawOval(50, 50, W - 100, H - 100);
    }

    protected class Canvas3D extends AWTGLCanvas {
        @Serial
        private static final long serialVersionUID = 1L;

        public Canvas3D(GLData data) {
            super(data);
        }

        public void initGL() {
            System.out.println("OpenGL version: " + effective.majorVersion + "." + effective.minorVersion + " (Profile: " + effective.profile + ")");
            createCapabilities();
            glClearColor(0.3f, 0.4f, 0.5f, 1);
        }

        @Override
        public void paintGL() {
            int w = W - 200;
            int h = H - 200;
            float aspect = (float) w / h;
            double now = System.currentTimeMillis() * 0.001;
            float width = (float) Math.abs(Math.sin(now * 0.3));
            glClear(GL_COLOR_BUFFER_BIT);
            glViewport(0, 0, w, h);
            glBegin(GL_QUADS);
            glColor3f(0.4f, 0.6f, 0.8f);
            glVertex2f(-0.75f * width / aspect, 0.0f);
            glVertex2f(0, -0.75f);
            glVertex2f(+0.75f * width / aspect, 0);
            glVertex2f(0, +0.75f);
            glEnd();
            swapBuffers();
        }
    }
}