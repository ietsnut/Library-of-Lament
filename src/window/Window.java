package window;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class Window {

    public final long handle;
    public final String title;

    public final int width;
    public final int height;

    public GLCapabilities capabilities;
    public boolean shouldClose = false;

    public Window(long handle, String title, int width, int height) {
        this.handle = handle;
        this.title = title;
        this.width = width;
        this.height = height;
    }

    public void makeContextCurrent() {
        glfwMakeContextCurrent(handle);
        GL.setCapabilities(capabilities);
    }

}
