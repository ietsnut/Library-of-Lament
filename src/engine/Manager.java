package engine;

import org.joml.Vector3f;
import scene.Train;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Manager {

    public static long  TIME;
    public static long  RATE;
    public static float PLAYTIME;
    public static Scene SCENE;

    // Window management
    public static Window[] windows = new Window[10]; // Initialize with max 10 windows
    private static int windowCount = 0;

    public static Callback debugProc;
    public static Process jwindow;

    // Window class to encapsulate window-specific data
    public static class Window {
        public final long handle;
        public final String name;
        public final int width;
        public final int height;
        public GLCapabilities capabilities;
        public boolean shouldClose = false;

        public Window(long handle, String name, int width, int height) {
            this.handle = handle;
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public void makeContextCurrent() {
            glfwMakeContextCurrent(handle);
            if (capabilities != null) {
                GL.setCapabilities(capabilities);
            }
        }
    }

    public static void run() {
        try {
            open();
            loop();
        } finally {
            close();
            System.exit(0);
        }
    }

    public static void open() {
        Console.log("Architecture", System.getProperty("os.arch"));
        Console.log("Version", Version.getVersion());

        GLFWErrorCallback errorfun = GLFWErrorCallback.createPrint();
        glfwSetErrorCallback(errorfun);

        if (!glfwInit()) {
            Console.error("Unable to initialize GLFW");
        }

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidmode == null) {
            Console.error("Failed to get video mode");
        }
        RATE = vidmode.refreshRate();

        // Create primary window (main game window)
        int primaryWidth = vidmode.height() * 4 / 5;
        int primaryHeight = primaryWidth;
        windows[0] = createWindow(0, "Library of Lament", primaryWidth, primaryHeight, "Main Game");

        if (windows[0] == null) {
            Console.error("Failed to create primary window!");
            return;
        }

        // Position primary window
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(windows[0].handle, pWidth, pHeight);
            glfwSetWindowPos(windows[0].handle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2);
        }

        // Setup OpenGL context for primary window
        windows[0].makeContextCurrent();
        GL.createCapabilities();
        windows[0].capabilities = GL.getCapabilities();

        // Setup debug output (only once, shared across contexts)
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            debugProc = GLUtil.setupDebugMessageCallback();
            glEnable(GL_DEBUG_OUTPUT);
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
            glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_NOTIFICATION,
                    (IntBuffer) null, false);
        } else {
            Console.warning("OpenGL 4.3 not supported. Debug output disabled.");
        }

        Console.log("Starting...");
        Renderer.init();

        // Set initial scenes
        SCENE = new Train();

        Resource.process();
        Camera.listen();

        // Show primary window
        glfwShowWindow(windows[0].handle);
    }

    // Create a new window
    public static Window createWindow(int i, String name, int width, int height, String title) {

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);
        glfwWindowHint(GLFW_FOCUSED, name.equals("primary") ? GLFW_TRUE : GLFW_FALSE);

        if (glfwGetPlatform() == GLFW_PLATFORM_COCOA) {
            glfwWindowHint(GLFW_COCOA_GRAPHICS_SWITCHING, GLFW_TRUE);
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        }

        // For primary window, no shared context. For others, share with primary
        long sharedContext = (i == 0) ? NULL : windows[0].handle;
        long windowHandle = glfwCreateWindow(width, height, title, NULL, sharedContext);

        if (windowHandle == NULL) {
            Console.error("Failed to create GLFW window: " + name);
            return null;
        }

        windows[i] = new Window(windowHandle, name, width, height);
        windowCount = Math.max(windowCount, i + 1);

        // Setup callbacks for the new window
        Control.listen(windowHandle);

        // If this isn't the primary window, create its OpenGL context
        if (i != 0 && windows[0] != null) {
            windows[i].makeContextCurrent();
            windows[i].capabilities = GL.createCapabilities();
        }

        Console.log("Created window: " + name + " (" + width + "x" + height + ")");
        return windows[i];
    }

    // Create a map window
    public static void createMapWindow() {
        Window mapWindow = createWindow(1, "map", 400, 300, "Map View");
        if (mapWindow != null) {
            // Position map window to the right of primary window
            glfwSetWindowPos(mapWindow.handle,
                    windows[0].width + 100, 100);

            glfwShowWindow(mapWindow.handle);
        }
    }

    private static void loop() {
        int fps = 0;
        long lastFrameTime = time();

        while (!shouldCloseAnyWindow()) {
            TIME = time();
            PLAYTIME = (TIME - lastFrameTime) / 1000f;

            if (TIME - lastFrameTime > 1000) {
                glfwSetWindowTitle(windows[0].handle, Integer.toString(fps));
                fps = 0;
                lastFrameTime += 1000;
                System.gc();
            }
            fps++;

            Resource.process();

            // Render all windows
            for (int i = 0; i < windowCount; i++) {
                Window window = windows[i];
                if (window == null || window.shouldClose) continue;

                window.makeContextCurrent();

                // Set viewport for this window
                glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                glViewport(0, 0, window.width, window.height);
                glDepthRange(0.0, 1.0);

                // Render the scene for this window
                if (SCENE != null) {
                    Renderer.render(SCENE);
                }

                glfwSwapBuffers(window.handle);

                // Check if this window should close
                if (glfwWindowShouldClose(window.handle)) {
                    window.shouldClose = true;
                }
            }

            glfwPollEvents();
        }
    }

    private static boolean shouldCloseAnyWindow() {
        // Close all if primary window should close
        if (windows[0] != null) {
            return glfwWindowShouldClose(windows[0].handle);
        }
        return true;
    }

    public static void close() {
        Console.log("Closing...");

        // IMPORTANT: Make primary context current before cleaning up OpenGL resources
        if (windows[0] != null) {
            windows[0].makeContextCurrent();
        }

        // Now clean up OpenGL resources while context is active
        Control.clear();
        Machine.clear();
        Resource.clear();
        FBO.unload();
        Shader.clear();

        // Close all windows
        for (int i = 0; i < windowCount; i++) {
            if (windows[i] != null) {
                //glfwFreeCallbacks(windows[i].handle);
                glfwDestroyWindow(windows[i].handle);
                windows[i] = null;
            }
        }

        if (debugProc != null) {
            debugProc.free();
        }

        glfwTerminate();
    }

    public static void stop() {
        for (int i = 0; i < windowCount; i++) {
            if (windows[i] != null) {
                glfwSetWindowShouldClose(windows[i].handle, true);
            }
        }
    }

    public static long time() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }
}