package game;

import engine.*;

import object.FBO;
import org.lwjgl.Version;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import resource.Resource;
import window.Window;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Manager {

    public static int   WIDTH = 800;
    public static int   HEIGHT = 800;
    public static long  RATE;

    public static long  window;

    public static Callback debugProc;

    public static void run() {
        try {
            open();
            loop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
            System.exit(0);
        }
    }

    public static void open() {
        Console.log("Architecture", System.getProperty("os.arch"));
        Console.log("Version", Version.getVersion());
        //Configuration.DEBUG.set(true);
        //Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
        //GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidmode == null) {
            throw new RuntimeException("Failed to get video mode");
        }
        RATE = vidmode.refreshRate();
        HEIGHT = vidmode.height();
        WIDTH = HEIGHT / 3 * 4;
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        window = glfwCreateWindow(WIDTH, HEIGHT, "", NULL, NULL);
        if (window == NULL) {
            Console.error("Failed to create GLFW window.");
            System.exit(0);
        }
        Console.log("OpenGL", glfwGetWindowAttrib(Manager.window, GLFW_CONTEXT_VERSION_MAJOR) + "." + glfwGetWindowAttrib(Manager.window, GLFW_CONTEXT_VERSION_MINOR));
        Control.listen(window);
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetWindowSize(window, pWidth, pHeight);
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
            glfwMakeContextCurrent(window);
            glfwSwapInterval(1);
            glfwShowWindow(window);
            GL.createCapabilities();
            //debugProc = GLUtil.setupDebugMessageCallback();
        }
        Console.log("Starting...");
        Window window1 = new Window(800, 800, "adder");
        Renderer.init();
        Resource.process();
    }

    private static void loop() {
        int fps = 0;
        long lastFrameTime = time();
        while (!glfwWindowShouldClose(window)) {
            if (time() - lastFrameTime > 1000) {
                glfwSetWindowTitle(window, Integer.toString(fps));
                fps = 0;
                lastFrameTime += 1000;
            }
            fps++;
            Resource.process();
            Renderer.render();
            glfwPollEvents();
            glfwSwapBuffers(window);
        }
    }

    public static void close() {
        Console.log("Closing...");
        Serial.close();
        Control.clear();
        Resource.clear();
        FBO.unload();
        Shader.clear();
        if (debugProc != null) {
            debugProc.free();
        }
        glfwDestroyWindow(window);
        glfwTerminate();
        //glfwSetErrorCallback(null).free();
    }

    public static void stop() {
        glfwSetWindowShouldClose(window, true);
    }

    public static long time() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }

}
