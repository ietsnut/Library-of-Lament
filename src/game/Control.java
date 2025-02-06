package game;

import object.Camera;
import org.lwjgl.glfw.*;
import scene.Board;
import scene.Forest;

import static org.lwjgl.glfw.GLFW.*;

public class Control {
    private static final boolean[] keys = new boolean[65536];
    private static float mouseX, mouseY, prevMouseX, prevMouseY, deltaMouseX, deltaMouseY, dWheel;
    private static boolean mouseLocked, firstMouse, clicked, holding;

    private static GLFWKeyCallback keyCallback;
    private static GLFWMouseButtonCallback mouseButtonCallback;
    private static GLFWScrollCallback scrollCallback;
    private static GLFWCursorPosCallback cursorPosCallback;

    public static void listen(long window) {
        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key < 0) {
                    return;
                }
                keys[key] = action != GLFW_RELEASE;
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (key == GLFW_KEY_ENTER && action == GLFW_RELEASE) {
                    Manager.swap();
                }
                if (key == GLFW_KEY_0 && action == GLFW_RELEASE) {
                    System.out.println(Math.round(Camera.position.x) + ", " + Math.round(Camera.position.y) + ", "  + Math.round(Camera.position.z));
                }
            }
        };
        glfwSetKeyCallback(window, keyCallback);

        mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button == 0 && action == 1 && !mouseLocked) {
                    glfwSetCursorPos(Manager.window, Manager.WIDTH/2f, Manager.HEIGHT/2f);
                    glfwSetInputMode(Manager.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    mouseLocked = true;
                    firstMouse = true;
                }
                if (button == 1 && action == 1 && mouseLocked) {
                    glfwSetInputMode(Manager.window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    glfwSetCursorPos(Manager.window, Manager.WIDTH/2f, Manager.HEIGHT/2f);
                    mouseLocked = false;
                    firstMouse = true;
                }
                if (mouseLocked && !firstMouse && button == 0 && action == 1) {
                    clicked = true;
                }
                if (mouseLocked && !firstMouse && button == 0 && action == GLFW_PRESS) {
                    holding = true;
                }
                if (mouseLocked && !firstMouse && button == 0 && action == GLFW_RELEASE) {
                    holding = false;
                }
            }
        };
        glfwSetMouseButtonCallback(window, mouseButtonCallback);

        scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                dWheel = (float) yoffset;
            }
        };
        glfwSetScrollCallback(window, scrollCallback);

        cursorPosCallback = new GLFWCursorPosCallback() {
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
                    deltaMouseX += mouseX - prevMouseX;
                    deltaMouseY += mouseY - prevMouseY;
                }
            }
        };
        glfwSetCursorPosCallback(window, cursorPosCallback);
    }

    public static void clear() {
        if (keyCallback != null) keyCallback.free();
        if (mouseButtonCallback != null) mouseButtonCallback.free();
        if (scrollCallback != null) scrollCallback.free();
        if (cursorPosCallback != null) cursorPosCallback.free();
    }

    public static boolean isClicked() {
        if (clicked) {
            clicked = false;
            return true;
        }
        return false;
    }

    public static boolean isHolding() {
        return holding;
    }

    public static boolean isKeyDown(int keycode) {
        return keys[keycode];
    }

    public static float dx() {
        float dx = deltaMouseX;
        deltaMouseX = 0;
        return dx;
    }

    public static float dy() {
        float dy = deltaMouseY;
        deltaMouseY = 0;
        return dy;
    }

    public static float getDWheel() {
        float dw = dWheel;
        dWheel = 0;
        return dw;
    }
}