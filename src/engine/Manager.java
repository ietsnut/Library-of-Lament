package engine;

<<<<<<< HEAD:src/engine/Manager.java
<<<<<<< HEAD:src/engine/Manager.java
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Configuration;
import shader.*;
=======
=======
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java
import scene.Board;
import engine.*;
import object.*;
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java

import object.FBO;
import org.lwjgl.Version;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import org.lwjgl.glfw.*;
import resource.Resource;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Manager {

    public static int   WIDTH = 800;
    public static int   HEIGHT = 800;
    public static long  RATE;

<<<<<<< HEAD:src/engine/Manager.java
<<<<<<< HEAD:src/engine/Manager.java
=======
    public static Scene scene;
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java
=======
    public static Scene scene;
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java
    public static long  window;

    public static Callback debugProc;

    public static Process jwindow;

    public static void run() {
        try {
            open();
            loop();
        } catch (Exception e) {
<<<<<<< HEAD:src/engine/Manager.java
<<<<<<< HEAD:src/engine/Manager.java
            e.printStackTrace();
=======
            System.err.println("Error during execution:");
            throw new RuntimeException(e);
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java
=======
            System.err.println("Error during execution:");
            throw new RuntimeException(e);
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java
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
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            glfwWindowHint(GLFW_COCOA_GRAPHICS_SWITCHING, GLFW_TRUE);
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        }
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
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();
        debugProc = GLUtil.setupDebugMessageCallback();
        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glDebugMessageControl(
                GL_DONT_CARE,                       // Source
                GL_DONT_CARE,                       // Type
                GL_DEBUG_SEVERITY_NOTIFICATION,     // Severity to filter out
                (IntBuffer)null,                    // IDs (none)
                false                               // false = disable
        );
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "java",
                        "-cp", System.getProperty("java.class.path"),
                        "window.SwingLauncher"
                );
                jwindow = pb.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (jwindow.isAlive()) {
                jwindow.destroy();
            }
        }));
        Console.log("Starting...");
        Serial.start();
    }

    private static void loop() {
        GL.createCapabilities();
        Renderer.init();
        Resource.process();
        while (!glfwWindowShouldClose(window)) {
<<<<<<< HEAD:src/engine/Manager.java
            glfwWaitEvents();
=======
            TIME = time();
            PLAYTIME = (TIME - lastFrameTime) / 1000f;
            if (TIME - lastFrameTime > 1000) {
                glfwSetWindowTitle(window, Integer.toString(fps));
                fps = 0;
                lastFrameTime += 1000;
                System.gc();
            }
            fps++;
<<<<<<< HEAD:src/engine/Manager.java
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java
=======
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java
            Resource.process();
            Renderer.render();
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
        //Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

<<<<<<< HEAD:src/engine/Manager.java
<<<<<<< HEAD:src/engine/Manager.java
    public static void stop() {
        glfwSetWindowShouldClose(window, true);
    }
=======
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java
=======
>>>>>>> parent of 9f378cf (basic scene switching):src/game/Manager.java

    public static long time() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }


}
