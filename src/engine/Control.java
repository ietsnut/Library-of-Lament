package engine;

import org.lwjgl.glfw.*;

import static org.lwjgl.glfw.GLFW.*;

public class Control {

    private static float mouseX, mouseY, prevMouseX, prevMouseY, deltaMouseX, deltaMouseY;
    private static boolean mouseLocked, firstMouse;

    private static GLFWKeyCallback keyCallback;
    private static GLFWMouseButtonCallback mouseButtonCallback;
    private static GLFWCursorPosCallback cursorPosCallback;

    public static void listen(long window) {
        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key < 0) {
                    return;
                }
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (key == GLFW_KEY_TAB && action == GLFW_PRESS && mouseLocked) {
                    glfwSetInputMode(Manager.window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    glfwSetCursorPos(Manager.window, Manager.WIDTH/2f, Manager.HEIGHT/2f);
                    mouseLocked = false;
                    firstMouse = true;
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
                    Manager.jwindow.toFront();
                }
                if (mouseLocked && !firstMouse && action == 1) {
                    if (button == 1) {
                        Serial.write();
                    } else if (button == 0) {
                        Serial.read();
                    }
                }
            }
        };
        glfwSetMouseButtonCallback(window, mouseButtonCallback);

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
        if (cursorPosCallback != null) cursorPosCallback.free();
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

}