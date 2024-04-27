package game;

import engine.*;
import object.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.system.MemoryStack;
import property.Load;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    public static final String TITLE = "";

    public static List<Scene>   scenes = new ArrayList<>();
    public static Renderer      renderer;
    public static Scene         scene;
    public static long          window;

    public static void run() {
        open();
        loop();
        close();
    }

    public static void open() {
        System.out.println("LWJGL " + Version.getVersion());
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        glfwSetKeyCallback(window, new Control.Keyboard());
        Control.listen();
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
            glfwMakeContextCurrent(window);
            glfwSwapInterval(1);
            glfwShowWindow(window);
            GL.createCapabilities();
        }
        scene = new Scene();
        scenes.add(scene);
        renderer = new Renderer();
    }

    private static void loop() {
        int fps = 0;
        long lastFrameTime = time();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        while ( !glfwWindowShouldClose(window) ) {
            //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            Load load = Load.process();
            if (load instanceof Terrain terrain) {
                scene.terrain = terrain;
            } else if (load instanceof Entity entity) {
                scene.entities.add(entity);
            }
            if (time() - lastFrameTime > 1000) {
                glfwSetWindowTitle(window, "FPS: " + fps);
                fps = 0;
                lastFrameTime += 1000;
            }
            fps++;
            glfwPollEvents();
            renderer.render(scene);
            glfwSwapBuffers(window);
        }
    }

    public static void close() {
        for (Load load : Load.BOUND) {
            load.unload();
        }
        Shader.unload();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static long time() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }

}
