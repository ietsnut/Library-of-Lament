package engine;

import object.Camera;
import org.lwjgl.glfw.*;

import static org.lwjgl.glfw.GLFW.*;

public class Control {
    private static final boolean[] keys = new boolean[65536];
    private static float mouseX, mouseY, prevMouseX, prevMouseY, deltaMouseX, deltaMouseY, dWheel;
    private static boolean mouseLocked, firstMouse, holding;

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
            }
        };
        glfwSetKeyCallback(window, keyCallback);

        mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button == 0 && action == 1 && !mouseLocked) {
                    glfwSetCursorPos(window, Manager.main.width/2f, Manager.main.height/2f);
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    mouseLocked = true;
                    firstMouse = true;
                }
                if (button == 1 && action == 1 && mouseLocked) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    glfwSetCursorPos(window, Manager.main.width/2f, Manager.main.height/2f);
                    mouseLocked = false;
                    firstMouse = true;
                }
                if (mouseLocked && !firstMouse && button == 0 && action == 1) {
                    Camera.click();
                    //Manager.map.open();
                    //Manager.text.open();
                    Console.debug(String.format("(%.1f, %.1f, %.1f)", Camera.position.x, Camera.position.y, Camera.position.z));
                    glfwFocusWindow(Manager.main.handle);
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
                if (!mouseLocked || Camera.resetting) return;
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

    public static void resetMouse() {
        deltaMouseX = 0;
        deltaMouseY = 0;

        // Reset mouse position to center
        glfwSetCursorPos(Manager.main.handle, Manager.main.width / 2.0, Manager.main.height / 2.0);

        // Force firstMouse true so the next movement doesn't apply a delta
        firstMouse = true;
    }

    public static void clear() {
        if (keyCallback != null) keyCallback.free();
        if (mouseButtonCallback != null) mouseButtonCallback.free();
        if (scrollCallback != null) scrollCallback.free();
        if (cursorPosCallback != null) cursorPosCallback.free();
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