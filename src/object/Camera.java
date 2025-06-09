package object;

import engine.Console;
import org.joml.*;
import org.joml.Math;
import property.*;
import engine.Control;
import engine.Manager;
import resource.Mesh;
import window.Main;

import static org.lwjgl.glfw.GLFW.*;

public class Camera implements Machine {

    public static final Matrix view         = new Matrix();
    public static final Matrix projection   = new Matrix();

    public static final Quaternionf orientation = new Quaternionf();
    public static final Vector3f rotation = new Vector3f(0);
    public static final Vector3f position = new Vector3f(0, 1.7f, 0);

    public static final float RANGE = 7.5f;
    public static final float DEFAULT_FOV = 90;
    public static final float SPEED = 0.015f;
    public static final float SENS  = 0.15f;
    public static final float SLOPE = 0.7071f;
    public static final float NEAR  = 0.1f;
    public static final float FAR   = Byte.MAX_VALUE * 2;

    private static float FOV = 90;
    private static float BOB = 0;

    public static Entity intersecting   = null;
    public static Entity inside         = null;

    private static final Vector3f resetTarget = new Vector3f(0, 1.7f, 0);
    private static final Vector3f resetRotation = new Vector3f(0);
    private static final Quaternionf resetOrientation = new Quaternionf().identity();

    public static boolean resetting = false;
    private static float resetProgress = 0.0f;
    private static final float RESET_SPEED = 3.0f;

    private static final Vector3f startPosition = new Vector3f();
    private static final Vector3f startRotation = new Vector3f();
    private static final Quaternionf startOrientation = new Quaternionf();

    public static void moveTo(Vector3f targetPosition, Vector3f targetRotation) {
        resetTarget.set(targetPosition);
        resetRotation.set(targetRotation);
        resetProgress = 0.0f;
        resetting = true;
        startOrientation.set(orientation);
        startPosition.set(position);
        startRotation.set(rotation);
    }

    public static void moveTo(Vector3f targetPosition) {
        moveTo(targetPosition, rotation);
    }

    public void turn() {
        update();
        updateView();
        updateProjection();
    }

    private static final Vector3f forward = new Vector3f();

    public static void update() {

        if (resetting) {
            resetProgress += 1.0f / Manager.RATE;
            float linearT = Math.min(resetProgress / RESET_SPEED, 1.0f);
            float t = (float)(1 - Math.cos(linearT * Math.PI)) * 0.5f;
            position.set(startPosition).lerp(resetTarget, t);
            rotation.set(startRotation).lerp(resetRotation, t);
            orientation.set(startOrientation).slerp(resetOrientation, t);
            updateView();
            updateProjection();
            if (linearT >= 1.0f) {
                resetting = false;
                rotation.set(resetRotation);
                orientation.set(resetOrientation);
                position.set(resetTarget);
                Control.resetMouse();
            } else {
                return;
            }
        }
        if (glfwGetInputMode(Manager.main.handle, GLFW_CURSOR) != GLFW_CURSOR_DISABLED) {
            FOV = Math.clamp(DEFAULT_FOV, 110, FOV - (20f / Manager.RATE));
            return;
        }
        rotation.add(0, Control.dx() * -SENS, 0);
        rotation.add(Control.dy() * -SENS, 0, 0);
        rotation.x = Math.min(Math.max(rotation.x, -80), 80);
        Camera.forward.set(forward()).setComponent(1, 0).normalize();
        Vector3f position = new Vector3f(Camera.position);
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

        if (Main.scene.terrain != null) {
            Terrain terrain = Main.scene.terrain;
            if (terrain.meshes[0].loaded()) {
                position = terrain.height(position, new Vector3f(movement));
            }
        }

        for (Entity entity : Main.scene.background) {
            entity.update();
        }

        float distance = Float.MAX_VALUE;
        Entity intersecting = null;
        boolean inside = false;
        for (Entity entity : Main.scene.foreground) {
            entity.update();
            if (entity instanceof Interactive interactive) {
                double collision = collision(position, entity);
                if (Math.signum(collision) == 0) {
                    if (Camera.inside == null) {
                        Camera.inside = entity;
                        interactive.enter();
                    }
                    inside = true;
                }
                float dist = position.distance(entity.position);
                if (collision > 0 && dist < RANGE && dist < distance) {
                    distance = dist;
                    intersecting = entity;
                }
            }
        }
        Camera.intersecting = intersecting;
        if (!inside) {
            if (Camera.inside instanceof Interactive interactive) {
                interactive.leave();
            }
            Camera.inside = null;
        }
        if (!Float.isNaN(position.x) && !Float.isNaN(position.z) && !Float.isNaN(position.y)) {
            Camera.position.set(position);
        }
        if (movement.length() > 0) {
            if (BOB <= 0.0f) {
                BOB = 360.0f;
            } else {
                BOB -= 7.5f;
            }
            Camera.position.y += Math.sin(Math.toRadians(BOB)) / 30.0f;
            FOV = Math.clamp(DEFAULT_FOV, 110, FOV + (20f / Manager.RATE));
        } else {
            FOV = Math.clamp(DEFAULT_FOV, 110, FOV - (20f / Manager.RATE));
        }
    }

    public static void click() {
        if (intersecting != null && intersecting instanceof Interactive interactive) {
            interactive.click();
        }
    }

    private static void updateView() {
        orientation.identity().rotateYXZ(Math.toRadians(rotation.y), Math.toRadians(rotation.x), Math.toRadians(rotation.z));
        view.inactive().identity().rotate(orientation.conjugate(new Quaternionf())).translate(-position.x, -position.y, -position.z);
        view.swap();
    }

    public static void updateProjection() {
        projection.inactive().identity().perspective(Math.toRadians(Camera.FOV), (float) Manager.main.width / (float) Manager.main.height, NEAR, FAR);
        projection.swap();
    }

    public static void listen() {

        Camera camera = new Camera();
        camera.start(Manager.RATE * 2);
        //Thread thread = new Thread(new Camera());
        //thread.start();
    }

/*
    @Override
    public void run() {
        while (true)
            turn();
    }*/

    public static Vector3f forward() {
        return orientation.transform(new Vector3f(0, 0, 1));
    }

    private static final Vector3f rayOrigin = new Vector3f();
    private static final Vector3f rayDir = new Vector3f();

    public static double collision(Vector3f camera, Entity entity) {
        Mesh mesh = entity.meshes[entity.state];
        Mesh.Collider collider = mesh.collider;

        if (collider == null || collider.min == null || collider.max == null) {
            return -1.0f;
        }

        // Reuse static temporary objects to reduce allocation
        Matrix4f inverseModelMatrix = new Matrix4f(entity.model.get()).invert();
        rayOrigin.set(Camera.position);
        rayDir.set(Camera.forward()).negate().normalize();

        // Transform to model space
        inverseModelMatrix.transformPosition(rayOrigin);
        inverseModelMatrix.transformDirection(rayDir).normalize();

        // Fail early if rayDir has zero component (avoid division by zero)
        if (rayDir.x == 0 || rayDir.y == 0 || rayDir.z == 0) {
            return -1.0f;
        }

        float invDirX = 1.0f / rayDir.x;
        float invDirY = 1.0f / rayDir.y;
        float invDirZ = 1.0f / rayDir.z;

        float tminX = (collider.min.x - rayOrigin.x) * invDirX;
        float tmaxX = (collider.max.x - rayOrigin.x) * invDirX;
        if (invDirX < 0) {
            float tmp = tminX;
            tminX = tmaxX;
            tmaxX = tmp;
        }

        float tminY = (collider.min.y - rayOrigin.y) * invDirY;
        float tmaxY = (collider.max.y - rayOrigin.y) * invDirY;
        if (invDirY < 0) {
            float tmp = tminY;
            tminY = tmaxY;
            tmaxY = tmp;
        }

        float tminZ = (collider.min.z - rayOrigin.z) * invDirZ;
        float tmaxZ = (collider.max.z - rayOrigin.z) * invDirZ;
        if (invDirZ < 0) {
            float tmp = tminZ;
            tminZ = tmaxZ;
            tmaxZ = tmp;
        }

        float tmin = Math.max(tminX, Math.max(tminY, tminZ));
        float tmax = Math.min(tmaxX, Math.min(tmaxY, tmaxZ));

        if (tmax < 0 || tmin > tmax) {
            return -1.0f; // No intersection
        }

        return Math.max(tmin, 0.0f); // 0 if inside
    }

}
