package object;

import engine.Renderer;
import game.Control;
import game.Game;
import game.Scene;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import property.Transformation;
import property.Worker;

import static org.lwjgl.glfw.GLFW.*;
import static property.Transformation.*;

public class Camera implements Worker {

    public static Transformation transformation     = new Transformation(1, 1, 1.5f);

    public static final Matrix4f projection = new Matrix4f();
    public static final Matrix4f view       = new Matrix4f();

    public static final float SPEED = 5f / RATE;
    public static final float SENS  = 10f / RATE;
    public static final float SLOPE = 0.7071f;
    public static final float NEAR  = 0.1f;
    public static final float FAR   = Byte.MAX_VALUE;

    public static float FOV = 75;

    private static float bob = 0;

    @Override
    public void work() {
        translate();
        rotate();
        view();
        projection();
    }

    public static void translate() {

        if (glfwGetInputMode(Game.window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {

            Vector3f forward = new Vector3f(transformation.forward().x, 0, transformation.forward().z).normalize();

            Vector3f origin = new Vector3f(transformation.position);
            Vector3f movement = new Vector3f();

            if (Control.isKeyDown(GLFW_KEY_W) || Control.isKeyDown(GLFW_KEY_UP)) {
                movement.add(forward.x * -SPEED, 0, forward.z * -SPEED);
            }
            if (Control.isKeyDown(GLFW_KEY_S) || Control.isKeyDown(GLFW_KEY_DOWN)) {
                movement.add(forward.x * SPEED, 0, forward.z * SPEED);
            }
            if (Control.isKeyDown(GLFW_KEY_D) || Control.isKeyDown(GLFW_KEY_RIGHT)) {
                movement.add(forward.z * SPEED, 0, forward.x * -SPEED);
            }
            if (Control.isKeyDown(GLFW_KEY_A) || Control.isKeyDown(GLFW_KEY_LEFT)) {
                movement.add(forward.z * -SPEED, 0, forward.x * SPEED);
            }
            if (Game.scene.terrain.bound()) {
                Vector3f position = Game.scene.terrain.height(origin, new Vector3f(movement));
                transformation.position.set(position);
                if (movement.length() > 0 && !position.equals(origin)) {
                    if (bob <= 0.0f) {
                        bob = 360.0f;
                    } else {
                        bob -= 10;
                    }
                    transformation.position.y += (float) Math.sin(Math.toRadians(bob)) / 20.0f;
                    FOV = Math.clamp(FOV + (20f / RATE), 75, 80);
                } else {
                    FOV = Math.clamp(FOV - (20f / RATE), 75, 80);
                }
            }

        }

    }

    public static void rotate() {
        transformation.rotation.add(0, Control.dx() * -SENS, 0);
        transformation.rotation.add(Control.dy() * -SENS, 0, 0);
        transformation.rotation.x = Math.min(Math.max(transformation.rotation.x, -80), 80);
    }

    public static void view() {
        Matrix4f view = new Matrix4f();
        transformation.orient();
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
        Camera.view.set(view);
    }

    public static void projection() {
        projection.set(new Matrix4f().identity().perspective((float) Math.toRadians(Camera.FOV), (float) Game.WIDTH / (float) Game.HEIGHT, NEAR, FAR));
    }

    public static void listen() {
        new Camera().start();
    }

}
