package object;

import property.Terrain;
import game.Control;
import game.Manager;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import property.Machine;

import static org.lwjgl.glfw.GLFW.*;

public class Camera implements Machine {

    public static final Matrix4f projection = new Matrix4f();
    public static final Matrix4f view = new Matrix4f();
    private static final Matrix4f viewBuffer = new Matrix4f();

    public static final Quaternionf orientation = new Quaternionf();
    public static final Vector3f rotation = new Vector3f(0, 0, 0);
    public static final Vector3f position = new Vector3f(0, 1f, 1.5f);

    public static final float SPEED = 2f / Manager.RATE;
    public static final float SENS = 10f / Manager.RATE;
    public static final float SLOPE = 0.7071f;
    public static final float NEAR = 0.1f;
    public static final float FAR = Byte.MAX_VALUE;

    public static float FOV = 75;

    private static float bob = 0;

    @Override
    public void turn() {
        update();
        view();
        projection();
    }

    public static void update() {
        if (glfwGetInputMode(Manager.window, GLFW_CURSOR) != GLFW_CURSOR_DISABLED) {
            FOV = Math.clamp(75, 90, FOV - (20f / Manager.RATE));
            return;
        }
        rotation.add(0, Control.dx() * -SENS, 0);
        rotation.add(Control.dy() * -SENS, 0, 0);
        rotation.x = Math.min(Math.max(rotation.x, -80), 80);
        Vector3f forward = new Vector3f(forward().x, 0, forward().z).normalize();
        Vector3f origin = new Vector3f(position);
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
        Terrain terrain = Manager.scene.terrain;
        if (terrain == null) return;
        Vector3f position = terrain.height(origin, new Vector3f(movement));
        Camera.position.set(position);
        if (movement.length() > 0 && !position.equals(origin)) {
            if (bob <= 0.0f) {
                bob = 360.0f;
            } else {
                bob -= 7.5f;
            }
            Camera.position.y += Math.sin(Math.toRadians(bob)) / 30.0f;
            FOV = Math.clamp(75, 90, FOV + (20f / Manager.RATE));
        } else {
            FOV = Math.clamp(75, 90, FOV - (20f / Manager.RATE));
        }
    }

    private static void view() {
        orient();
        viewBuffer.identity();
        viewBuffer.m00(1.0f - 2.0f * (orientation.y * orientation.y + orientation.z * orientation.z));
        viewBuffer.m01(2.0f * (orientation.x * orientation.y - orientation.z * orientation.w));
        viewBuffer.m02(2.0f * (orientation.x * orientation.z + orientation.y * orientation.w));
        viewBuffer.m03(0);
        viewBuffer.m10(2.0f * (orientation.x * orientation.y + orientation.z * orientation.w));
        viewBuffer.m11(1.0f - 2.0f * (orientation.x * orientation.x + orientation.z * orientation.z));
        viewBuffer.m12(2.0f * (orientation.y * orientation.z - orientation.x * orientation.w));
        viewBuffer.m13(0);
        viewBuffer.m20(2.0f * (orientation.x * orientation.z - orientation.y * orientation.w));
        viewBuffer.m21(2.0f * (orientation.y * orientation.z + orientation.x * orientation.w));
        viewBuffer.m22(1.0f - 2.0f * (orientation.x * orientation.x + orientation.y * orientation.y));
        viewBuffer.m23(0);
        viewBuffer.m30(0);
        viewBuffer.m31(0);
        viewBuffer.m32(0);
        viewBuffer.m33(1.0f);
        viewBuffer.translate(-position.x, -position.y, -position.z);
        view.set(viewBuffer);
    }

    public static void projection() {
        projection.identity().perspective(Math.toRadians(Camera.FOV), (float) Manager.WIDTH / (float) Manager.HEIGHT, NEAR, FAR);
    }

    public static void listen() {
        Camera camera = new Camera();
        camera.start(120);
    }

    private static void rot(float x, float y, float z, double angle) {
        Quaternionf deltaRotation = new Quaternionf().rotationAxis((float) angle, x, y, z);
        orientation.mul(deltaRotation);
    }

    private static void orient() {
        orientation.identity();
        rot(0, 1, 0, Math.toRadians(rotation.y));
        rot(1, 0, 0, Math.toRadians(rotation.x));
        rot(0, 0, 1, Math.toRadians(rotation.z));
    }

    public static Vector3f forward() {
        return orientation.transform(new Vector3f(0, 0, 1));
    }

}
