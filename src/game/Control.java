package game;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Control {

    private static float mouseX, mouseY, prevMouseX, prevMouseY;
    private static boolean leftButtonPressed, rightButtonPressed;
    private static float dWheel;

    public static class Keyboard extends GLFWKeyCallback {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key < 0) {
                return;
            }
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        }
    }

    public static void listen() {
        GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                leftButtonPressed = button == 0 && action == 1;
                rightButtonPressed = button == 1 && action == 1;
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
                prevMouseX = mouseX;
                prevMouseY = mouseY;
                mouseX = (float) xpos;
                mouseY = (float) (Game.HEIGHT - ypos);
            }
        };
        mouseButtonCallback.set(Game.window);
        scrollCallback.set(Game.window);
        cursorPosCallback.set(Game.window);
    }

    public static boolean isKeyDown(int keycode) {
        int state = glfwGetKey(Game.window, keycode);
        return state == GLFW_PRESS;
    }

    public static void update() {
        dWheel = 0;
    }

    public static float getX() {
        return mouseX;
    }

    public static float getY() {
        return mouseY;
    }

    public static float getDX() {
        float dx = mouseX - prevMouseX;
        prevMouseX = mouseX;
        return dx;
    }

    public static float getDY() {
        float dy = mouseY - prevMouseY;
        prevMouseY = mouseY;
        return dy;
    }

    public static boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public static boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    public static float getDWheel() {
        return dWheel;
    }
}
