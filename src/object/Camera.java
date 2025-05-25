package object;

import org.joml.*;
import org.joml.Math;
import property.*;
import engine.Control;
import engine.Manager;
import resource.Mesh;

import static org.lwjgl.glfw.GLFW.*;

public class Camera implements Machine {

    public static final Matrix view         = new Matrix();
    public static final Matrix projection   = new Matrix();

    public static final Quaternionf orientation = new Quaternionf();
    public static final Vector3f rotation = new Vector3f(0, 0, 0);
    public static final Vector3f position = new Vector3f(0, 1f, 1.5f);

    public static final float SPEED = 0.025f;
    public static final float SENS  = 0.15f;
    public static final float SLOPE = 0.7071f;
    public static final float NEAR  = 0.1f;
    public static final float FAR   = Byte.MAX_VALUE * 2;

    public static float FOV = 75;

    private static float bob = 0;

    public static Entity intersecting   = null;
    public static Entity inside         = null;

    public static void reset() {
        position.set(0, 1f, 0);
        orientation.set(0, 0, 0, 0);
        rotation.set(0, 0, 0);
        orient();
    }

    public void turn() {
        update();
        updateView();
        updateProjection();
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


        if (Manager.scene.terrain == null) return;
        Terrain terrain = Manager.scene.terrain;
        if (!terrain.meshes[0].loaded()) return;
        Vector3f position = terrain.height(origin, new Vector3f(movement));

        boolean inside = false;
        boolean stopped = false;
        for (Entity entity : Manager.scene.entities) {
            entity.update();
            if (entity instanceof Interactive interactive) {
                if (Camera.inside(position, entity)) {
                    if (Camera.inside == null) {
                        if (entity instanceof Solid) {
                            stopped = true;
                            break;
                        } else {
                            Camera.inside = entity;
                            interactive.enter();
                        }
                    }
                    inside = true;
                }
            }
        }
        if (!stopped && !Float.isNaN(position.x) && !Float.isNaN(position.z) && !Float.isNaN(position.y)) {
            Camera.position.set(position);
        }
        if (!inside) {
            if (Camera.inside instanceof Interactive interactive) {
                interactive.leave();
            }
            Camera.inside = null;
        }
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

    public static void click() {
        float distance = Float.MAX_VALUE;
        Entity intersecting = null;
        boolean inside = false;
        boolean stopped = false;
        for (Entity entity : Manager.scene.entities) {
            entity.update();
            if (entity instanceof Interactive interactive) {
                float dist = position.distance(entity.position);
                if (Camera.collision(position, entity) > 0 && dist < 7.5f && dist < distance) {
                    distance = dist;
                    intersecting = entity;
                }
            }
        }
        Camera.intersecting = intersecting;
        if (intersecting instanceof Interactive interactive) {
            interactive.click();
        }
    }

    private static void updateView() {
        Matrix4f viewBuffer = view.inactive();
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
        view.swap();
    }


    public static void updateProjection() {
        Matrix4f projectionBuffer = projection.inactive();
        projectionBuffer.identity().perspective(Math.toRadians(Camera.FOV), (float) Manager.WIDTH / (float) Manager.HEIGHT, NEAR, FAR);
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

    private static void rot(Vector3f axis, float angle) {
        Quaternionf deltaRotation = new Quaternionf().rotationAxis(angle, axis);
        orientation.mul(deltaRotation);
    }

    private static void orient() {
        orientation.identity();
        rot(Entity.Y, Math.toRadians(rotation.y));
        rot(Entity.X, Math.toRadians(rotation.x));
        rot(Entity.Z, Math.toRadians(rotation.z));
    }

    public static Vector3f forward() {
        return orientation.transform(new Vector3f(0, 0, 1));
    }

    public static double collision(Vector3f camera, Entity entity) {
        Mesh mesh = entity.meshes[entity.state];
        if (mesh.collider == null || mesh.collider.min == null || mesh.collider.max == null) {
            return -1f;
        }

        Mesh.Collider collider = mesh.collider;

        // to model space
        Matrix4f inverseModelMatrix = new Matrix4f(entity.model.get()).invert();
        Vector3f rayOriginModelSpace = inverseModelMatrix.transformPosition(new Vector3f(Camera.position));
        Vector3f rayDirectionModelSpace = inverseModelMatrix.transformDirection(Camera.forward().negate()).normalize();

        // inverse direction vector
        Vector3f invDir = new Vector3f(
                1.0f / rayDirectionModelSpace.x,
                1.0f / rayDirectionModelSpace.y,
                1.0f / rayDirectionModelSpace.z
        );

        // tmin and tmax for each axis
        float t1 = (collider.min.x - rayOriginModelSpace.x) * invDir.x;
        float t2 = (collider.max.x - rayOriginModelSpace.x) * invDir.x;
        float t3 = (collider.min.y - rayOriginModelSpace.y) * invDir.y;
        float t4 = (collider.max.y - rayOriginModelSpace.y) * invDir.y;
        float t5 = (collider.min.z - rayOriginModelSpace.z) * invDir.z;
        float t6 = (collider.max.z - rayOriginModelSpace.z) * invDir.z;

        // the nearest and farthest intersection distances
        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        // intersection validity
        if (tmax < 0 || tmin > tmax) {
            return -1f; // No intersection
        }

        return Math.max(tmin, 0); // Return the intersection distance (or 0 if the ray starts inside)
    }

    //camera is inside AABB
    public static boolean inside(Vector3f camera,Entity entity) {
        return collision(camera, entity) == 0;
    }

}
