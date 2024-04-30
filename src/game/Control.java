package game;

import object.Camera;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import property.Transformation;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Control {

    private static final boolean[] keys = new boolean[65536];
    private static float mouseX, mouseY, prevMouseX, prevMouseY, deltaMouseX, deltaMouseY;
    private static float dWheel;
    private static boolean mouseLocked, firstMouse;

    public static void listen(long window) {
        GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key < 0) {
                    return;
                }
                keys[key] = action != GLFW_RELEASE;
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
        };
        GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button == 0 && action == 1 && !mouseLocked) {
                    glfwSetCursorPos(Game.window, Game.WIDTH/2f, Game.HEIGHT/2f);
                    glfwSetInputMode(Game.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    mouseLocked = true;
                    firstMouse = true;
                }
                if (button == 1 && action == 1 && mouseLocked) {
                    glfwSetInputMode(Game.window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    glfwSetCursorPos(Game.window, Game.WIDTH/2f, Game.HEIGHT/2f);
                    mouseLocked = false;
                    firstMouse = true;
                }
            }
        };
        GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                dWheel = (float) yoffset;
            }
        };
        GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (!mouseLocked) return;
                if (firstMouse) {
                    prevMouseX = mouseX = (float) xpos;
                    prevMouseY = mouseY = (float) ypos;
                    firstMouse = false;
                } else {
                    prevMouseX = mouseX;
                    prevMouseY = mouseY;
                    mouseX = (float) xpos;
                    mouseY = (float) ypos;
                    deltaMouseX = mouseX - prevMouseX;
                    deltaMouseY = mouseY - prevMouseY;
                    Camera.rotate();
                }
            }
        };
        mouseButtonCallback.set(window);
        scrollCallback.set(window);
        cursorPosCallback.set(window);
        keyCallback.set(window);
    }

    public static boolean isKeyDown(int keycode) {
        return keys[keycode];
    }

    public static void update() {
        dWheel = 0;
    }

    public static float getDX() {
        return deltaMouseX;
    }

    public static float getDY() {
        return deltaMouseY;
    }

    public static float getDWheel() {
        return dWheel;
    }
}
