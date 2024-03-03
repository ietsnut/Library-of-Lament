package perspective;

import com.sun.jdi.BooleanType;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL40.*;

import javax.swing.*;

public class AWTTest {

    static BufferedImage image;

    public static void main(String[] args) {
        JFrame frame = new JFrame("AWT test");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(600, 600));
        GLData data = new GLData();
        AWTGLCanvas canvas;
        frame.add(canvas = new AWTGLCanvas(data) {
            private static final long serialVersionUID = 1L;
            @Override
            public void initGL() {
                System.out.println("OpenGL version: " + effective.majorVersion + "." + effective.minorVersion + " (Profile: " + effective.profile + ")");
                createCapabilities();
                glClearColor(0.3f, 0.4f, 0.5f, 1);
            }
            @Override
            public void paintGL() {
                int w = 600;
                int h = 600;
                float aspect = (float) w / h;
                double now = System.currentTimeMillis() * 0.001;
                float width = (float) Math.abs(Math.sin(now * 0.3));
                glClear(GL_COLOR_BUFFER_BIT);
                glViewport(0, 0, w, h);
                glBegin(GL_QUADS);
                glColor3f(0.4f, 0.6f, 0.8f);
                glVertex2f(-0.75f * width / aspect, 0.0f);
                glVertex2f(0, -0.75f);
                glVertex2f(+0.75f * width/ aspect, 0);
                glVertex2f(0, +0.75f);
                glEnd();

                swapBuffers();
                ByteBuffer nativeBuffer = BufferUtils.createByteBuffer(w*h*3);
                image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
                GL11.glReadPixels(0, 0, w, h, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, nativeBuffer);
                byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
                nativeBuffer.get(imgData);
            }
        }, BorderLayout.CENTER);

        image = new BufferedImage(600, 600, BufferedImage.TYPE_3BYTE_BGR);

        JFrame frame2 = new JFrame();
        frame2.setTitle("image");
        frame2.setSize(image.getWidth(), image.getHeight());
        frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JLabel label=new JLabel();
        label.setIcon(new ImageIcon(image));
        frame2.getContentPane().add(label,BorderLayout.CENTER);
        frame2.setLocationRelativeTo(null);
        frame2.pack();
        frame2.setVisible(true);

        frame.pack();
        frame.setVisible(true);
        frame.transferFocus();
        Runnable renderLoop = new Runnable() {
            @Override
            public void run() {
                if (!canvas.isValid()) {
                    GL.setCapabilities(null);
                    return;
                }
                canvas.render();

                SwingUtilities.invokeLater(this);
            }
        };
        SwingUtilities.invokeLater(renderLoop);
    }
}
