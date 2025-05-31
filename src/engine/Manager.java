package engine;

import scene.Driving;
import shader.*;
import object.*;

import org.lwjgl.Version;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import property.Machine;
import resource.Resource;
import scene.Forest;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Manager {

    public static int   WIDTH;
    public static int   HEIGHT;
    public static long  TIME;
    public static long  RATE;
    public static float PLAYTIME;

    public static long  window;

    public static Scene scene;

    public static Callback debugProc;

    public static Process jwindow;

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
        WIDTH = HEIGHT = vidmode.height() * 4 / 5;

        //WIDTH = vidmode.width() * 4 / 5;
        glfwDefaultWindowHints();
        //glfwWindowHint(GLFW_SAMPLES, 8);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);
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
        //GLCapabilities caps = GL.getCapabilities();
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            debugProc = GLUtil.setupDebugMessageCallback();
            glEnable(GL_DEBUG_OUTPUT);
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
            glDebugMessageControl(
                    GL_DONT_CARE,
                    GL_DONT_CARE,
                    GL_DEBUG_SEVERITY_NOTIFICATION,
                    (IntBuffer) null,
                    false
            );
        } else {
            Console.warning("OpenGL 4.3 not supported. Debug output disabled.");
        }
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
        //Serial.listen();

        Console.log("Starting...");
        Renderer.init();
        scene = new Driving();
        Resource.process();
        Camera.listen();
    }

    private static void loop() {
        int fps = 0;
        long lastFrameTime = time();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
        glDepthRange(0.0, 1.0);
        while (!glfwWindowShouldClose(window)) {
            //glfwWaitEvents();
            TIME = time();
            PLAYTIME = (TIME - lastFrameTime) / 1000f;
            if (TIME - lastFrameTime > 1000) {
                glfwSetWindowTitle(window, Integer.toString(fps));
                fps = 0;
                lastFrameTime += 1000;
                System.gc();
            }
            fps++;
            Resource.process();
            Renderer.render(scene);
            glfwPollEvents();
            glfwSwapBuffers(window);
        }
    }

    public static void close() {
        Console.log("Closing...");
        //Serial.close();
        Control.clear();
        Machine.clear();
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
