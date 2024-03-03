package perspective;
import org.lwjgl.opengl.*;
import org.lwjgl.glfw.*;
import java.nio.ByteBuffer;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class GLCanvasExample {
    private static long window;
    private static GLFWKeyCallback keyCallback;

    public static void main(String[] args) {
        try {
            init();
            createAndShowGUI();
            loop();

            // Terminate GLFW and release the GLFWErrorCallback
            GLFW.glfwTerminate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE); // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE); // the window will be resizable

        // Create the window
        window = GLFW.glfwCreateWindow(800, 600, "GLFW Window", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                    GLFW.glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
                }
            }
        };
        GLFW.glfwSetKeyCallback(window, keyCallback);

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window);
        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        // Make the window visible
        GLFW.glfwShowWindow(window);

    }

    private static void loop() {
        GL.createCapabilities();

        // Run the rendering loop until the user has attempted to close the window
        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // Render OpenGL content

            // Swap the color buffers
            GLFW.glfwSwapBuffers(window);

            // Poll for window events. The key callback above will only be invoked during this call.
            GLFW.glfwPollEvents();
        }
    }

    private static BufferedImage createImage() {
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        // Drawing on BufferedImage using Java2D
        g2d.setColor(Color.RED);
        g2d.fillRect(50, 50, 100, 100);
        g2d.dispose();
        return image;
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("GLFW Window with JPanel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create GLCanvas
        JPanel glPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Rendering OpenGL content
                g.setColor(Color.GREEN);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Create JPanel with Java2D content
        JPanel java2dPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Rendering Java2D content
                BufferedImage image = GLCanvasExample.createImage();
                g.drawImage(image, 0, 0, null);
            }
        };

        frame.setLayout(new BorderLayout());
        frame.add(glPanel, BorderLayout.CENTER);
        frame.add(java2dPanel, BorderLayout.CENTER); // Adding JPanel with Java2D content

        frame.setVisible(true);
    }
}