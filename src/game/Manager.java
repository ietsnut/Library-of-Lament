package game;

import scene.Sewer;
import engine.*;
import object.*;

import org.lwjgl.Version;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import property.Machine;
import resource.Resource;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Manager {

    public static int   WIDTH;
    public static int   HEIGHT;
    public static long  TIME;
    public static long  RATE;
    public static float PLAYTIME;

    public static Scene scene;
    public static long  window;

    public static Callback debugProc;

    public static void run() {
        try {
            open();
            loop();
        } finally {
            close();
        }
    }

    public static void open() {
        System.out.println("LWJGL " + Version.getVersion());
        GLFWErrorCallback.createPrint(System.err).set();
        System.out.println(1);
        if (!glfwInit()) {
            System.out.println(1.5);
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        System.out.println(2);
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidmode == null) {
            throw new RuntimeException("Failed to get video mode");
        }
        System.out.println(3);
        RATE = vidmode.refreshRate();
        WIDTH = vidmode.height() * 4 / 5;
        HEIGHT = vidmode.height() * 4 / 5;
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
            throw new RuntimeException("Failed to create the GLFW window");
        }
        Control.listen(window);
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetWindowSize(window, pWidth, pHeight);
            glfwSetWindowPos(window, (1000 - pWidth.get(0)) / 2, (1000 - pHeight.get(0)) / 2);
            glfwMakeContextCurrent(window);
            glfwSwapInterval(1);
            glfwShowWindow(window);
            GL.createCapabilities();
            debugProc = GLUtil.setupDebugMessageCallback();
        }

        scene = new Sewer();
        Renderer.init();
        Camera.listen();
    }

    private static void loop() {
        int fps = 0;
        long lastFrameTime = time();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
        glDepthRange(0.0, 1.0);
        while (!glfwWindowShouldClose(window)) {
            TIME = time();
            PLAYTIME = (TIME - lastFrameTime) / 1000f;
            if (TIME - lastFrameTime > 1000) {
                glfwSetWindowTitle(window, Integer.toString(fps));
                fps = 0;
                lastFrameTime += 1000;
            }
            fps++;
            //Camera.update();
            Resource.process();
            Renderer.render(scene);
            glfwPollEvents();
            glfwSwapBuffers(window);
        }
    }

    public static void close() {
        Machine.clear();
        Resource.clear();
        Shader.clear();
        if (debugProc != null) {
            debugProc.free();
        }
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }


    public static long time() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }


}
