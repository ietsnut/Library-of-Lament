package object;

import game.Control;
import game.Game;
import game.Scene;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import property.Transformation;

import static org.lwjgl.glfw.GLFW.*;

public class Camera extends Thread {

    /* TODO:
        - add head bobbing when walking (optional: maybe bob the tool)
        - add dynamic walking speed based on terrain
        - add jumping and gravity
        - add dynamic FOV
    */

    public static Transformation transformation     = new Transformation();
    public static Matrix4f view                     = new Matrix4f();

    public static final float SPEED    = 0.05f;
    public static final float SENS     = 0.2f;

    public static float FOV = 75;

    public static void move() {
        if (glfwGetInputMode(Game.window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
            Vector3f forward = new Vector3f(transformation.forward().x, 0, transformation.forward().z).normalize();
            if (Control.isKeyDown(GLFW_KEY_W) || Control.isKeyDown(GLFW_KEY_UP)) {
                transformation.position.add(forward.x * -SPEED, 0, forward.z * -SPEED);
            }
            if (Control.isKeyDown(GLFW_KEY_S) || Control.isKeyDown(GLFW_KEY_DOWN)) {
                transformation.position.add(forward.x * SPEED, 0, forward.z * SPEED);
            }
            if (Control.isKeyDown(GLFW_KEY_D) || Control.isKeyDown(GLFW_KEY_RIGHT)) {
                transformation.position.add(forward.z * SPEED, 0, forward.x * -SPEED);

            }
            if (Control.isKeyDown(GLFW_KEY_A) || Control.isKeyDown(GLFW_KEY_LEFT)) {
                transformation.position.add(forward.z * -SPEED, 0, forward.x * SPEED);
            }
            transformation.position.y = 1f;
        }
    }

    public static void rotate() {
        float dx        = Control.getDX() * -SENS;
        float dy        = Control.getDY() * -SENS;
        transformation.rotate(Transformation.Axis.Y, dx).rotate(Transformation.Axis.X, dy).rotation(Transformation.Axis.X, Math.min(Math.max(Camera.transformation.rotation.x, -80), 80));
    }

    public static void view() {
        transformation.norm();
        view.identity();
        view.m00(1.0f - 2.0f * (transformation.orientation.y * transformation.orientation.y + transformation.orientation.z * transformation.orientation.z));
        view.m01(2.0f * (transformation.orientation.x * transformation.orientation.y - transformation.orientation.z * transformation.orientation.w));
        view.m02(2.0f * (transformation.orientation.x * transformation.orientation.z + transformation.orientation.y * transformation.orientation.w));
        view.m03(0);
        view.m10(2.0f * (transformation.orientation.x * transformation.orientation.y + transformation.orientation.z * transformation.orientation.w));
        view.m11(1.0f - 2.0f * (transformation.orientation.x * transformation.orientation.x + transformation.orientation.z * transformation.orientation.z));
        view.m12(2.0f * (transformation.orientation.y * transformation.orientation.z - transformation.orientation.x * transformation.orientation.w));
        view.m13(0);
        view.m20(2.0f * (transformation.orientation.x * transformation.orientation.z - transformation.orientation.y * transformation.orientation.w));
        view.m21(2.0f * (transformation.orientation.y * transformation.orientation.z + transformation.orientation.x * transformation.orientation.w));
        view.m22(1.0f - 2.0f * (transformation.orientation.x * transformation.orientation.x + transformation.orientation.y * transformation.orientation.y));
        view.m23(0);
        view.m30(0);
        view.m31(0);
        view.m32(0);
        view.m33(1.0f);
        view.translate(-transformation.position.x, -transformation.position.y, -transformation.position.z);
    }


}
