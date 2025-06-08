package window;

import engine.Console;
import engine.Manager;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import resource.Mesh;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class Window {

    public final String title = this.getClass().getSimpleName().toLowerCase();

    public final long handle;

    public final int width;
    public final int height;

    public GLCapabilities capabilities;

    public volatile boolean open = this instanceof Main;
    public volatile boolean visible = false;

    public final Mesh quad;

    public Window(int width, int height) {

        this.width = width;
        this.height = height;

        glfwDefaultWindowHints();

        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);
        //glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        //glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, 8);
        glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);
        glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE);
        glfwWindowHint(GLFW_FOCUSED, this instanceof Main ? GLFW_TRUE : GLFW_FALSE);

        if (glfwGetPlatform() == GLFW_PLATFORM_COCOA) {
            glfwWindowHint(GLFW_COCOA_GRAPHICS_SWITCHING, GLFW_TRUE);
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        }

        long sharedContext = this instanceof Main ? NULL : Manager.main.handle;
        handle = glfwCreateWindow(width, height, title, NULL, sharedContext);

        if (handle == NULL) {
            Console.error("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(handle);
        capabilities = GL.createCapabilities();
        GL.setCapabilities(capabilities);

        this.quad = (Mesh) new Mesh() {
            @Override
            public void load() {
                vertices = new byte[] {-1, 1, -1, -1, 1, 1, 1, -1};
                indices  = new int[] {0, 1, 2, 2, 1, 3};
                uvs = new float[]{ 0, 0, 1, 0, 1, 1, 0, 1 };
            }
            @Override
            public int dimensions() {
                return 2;
            }
            @Override
            public String toString() {
                return "QUAD";
            }
        }.direct();

        if (this instanceof Main) {
            glfwShowWindow(handle);
        }

        Console.log("Created window", title + " (" + width + "x" + height + ")");
    }

    public Window(int size) {
        this(size, size);
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public void makeContextCurrent() {
        glfwMakeContextCurrent(handle);
        if (capabilities != null) {
            GL.setCapabilities(capabilities);
        }
    }

    abstract public void setup();
    abstract public void draw();

}
