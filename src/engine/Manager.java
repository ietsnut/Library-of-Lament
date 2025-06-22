package engine;

import resource.Calculation;
import resource.Mesh;
import resource.Mipmap;
import shader.*;

import org.lwjgl.Version;
import org.lwjgl.system.Callback;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import property.Machine;
import resource.Resource;
import window.*;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVGGL3.nvgDelete;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL43.*;

public class Manager {

    public static long RATE;
    public static long WIDTH;
    public static long HEIGHT;
    public static long DELTATIME = 0;

    public static Main main;
    public static Map map;
    public static Text text;

    private static Window[] windows;

    private static Callback debugProc;
    private static long previousTime = System.nanoTime();
    private static int error = GL_NO_ERROR;

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
        Console.debug("Architecture", System.getProperty("os.arch"));
        Console.debug("Version", Version.getVersion());

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
        WIDTH = vidmode.width();
        HEIGHT = vidmode.height();

        main = new Main((int) Math.min(WIDTH, HEIGHT));

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

        main.setup();

        map = new Map((int) HEIGHT * 4 / 5);
        map.setup();

        text = new Text((int) HEIGHT * 2 / 5);
        text.setup();

        windows = new Window[3];
        windows[0] = main;
        windows[1] = map;
        windows[2] = text;

    }

    private static void loop() {

        while (main != null && !glfwWindowShouldClose(main.handle)) {

            glfwPollEvents();

            for (int i = 0; i < windows.length; i++) {

                Window window = windows[i];

                if (window == null) continue;

                if (window.open && !window.visible) {
                    glfwShowWindow(window.handle);
                    glfwSetWindowShouldClose(window.handle, false);
                    window.visible = true;
                    Console.log("Showing window[" + i + "]", window.title);
                } else if (!window.open && window.visible) {
                    glfwHideWindow(window.handle);
                    window.visible = false;
                    Console.log("Hiding window[" + i + "]", window.title);
                    continue;
                }

                long currentTime = System.nanoTime();
                DELTATIME = currentTime - previousTime;
                previousTime = currentTime;

                window.makeContextCurrent();

                if (i == 0) Resource.process();

                glViewport(0, 0, window.width, window.height);
                glClearColor(0, 0, 0, 0);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                window.draw();

                error = glGetError();
                while (error != GL_NO_ERROR) {
                    Console.error("OpenGL", error, getGLErrorString(error));
                    error = glGetError();
                }

                glfwSwapBuffers(window.handle);

                if (glfwWindowShouldClose(window.handle)) {
                    window.close();
                    if (i == 0) {
                        Console.log("Main window closed");
                        return;
                    }
                }
            }
        }
    }

    public static void close() {

        Console.log("Closing...");

        if (main != null) {
            main.makeContextCurrent();
        }


        Control.clear();
        Machine.clear();
        Resource.clear();
        Calculation.shutdown();
        Shader.clear();

        for (int i = 0; i < windows.length; i++) {
            if (windows[i] != null) {
                windows[i].clear();
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
        for (Window window : windows) {
            if (window != null) {
                glfwSetWindowShouldClose(window.handle, true);
            }
        }
    }

    public static float time() {
        return (float) GLFW.glfwGetTime();
    }

    private static String getGLErrorString(int error) {
        return switch (error) {
            case GL_NO_ERROR -> "No error";
            case GL_INVALID_ENUM -> "Invalid enum";
            case GL_INVALID_VALUE -> "Invalid value";
            case GL_INVALID_OPERATION -> "Invalid operation";
            case GL_OUT_OF_MEMORY -> "Out of memory";
            case GL_STACK_UNDERFLOW -> "Stack underflow";
            case GL_STACK_OVERFLOW -> "Stack overflow";
            default -> "Unknown error";
        };
    }

}