package object;

import engine.Renderer;
import game.Scene;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector4f;
import property.Axis;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import property.Transformation;

public class Camera {

    public Transformation transformation = new Transformation();

    private float moveSpeed = 0.05f;
    private float mouseSensitivity = 0.1f;

    public float FOV = 75;

    public void update(Scene scene) {
        float dx = Mouse.getDX() * -mouseSensitivity;
        float dy = Mouse.getDY() * mouseSensitivity;
        if (!Mouse.isGrabbed()) {
            dx = 0;
            dy = 0;
        }
        transformation.rotate(Axis.Y, dx).rotate(Axis.X, dy).rotation(Axis.X, clamp(transformation.rotation.x, -80, 80));

        Vector3f forward = new Vector3f(transformation.forward().x, 0, transformation.forward().z);
        if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))      transformation.translate(forward.x * -moveSpeed, 0, forward.z * -moveSpeed);
        if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))    transformation.translate(forward.x * moveSpeed, 0, forward.z * moveSpeed);
        if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))   transformation.translate(forward.z * moveSpeed, 0, forward.x * -moveSpeed);
        if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT))    transformation.translate(forward.z * -moveSpeed, 0, forward.x * moveSpeed);
        transformation.position.y = scene.terrain.getHeightOfTerrain(transformation.position.x, transformation.position.z) + 2f;

        for (Entity entity : scene.entities) {
            Matrix4f model = entity.transformation.model();
            Vector3f rayOrigin = new Vector3f(transformation.position);
            Vector3f ray = transformation.forward();
            Vector3f rayDirection = new Vector3f(-ray.x, -ray.y, -ray.z).normalise(null);
            System.out.println("Direction X: " + Math.round(rayDirection.x) + ", Y: " + Math.round(rayDirection.y) + ", Z: " + Math.round(rayDirection.z));
            System.out.println("Origin X: " + rayOrigin.x + ", Y: " + rayOrigin.y + ", Z: " + rayOrigin.z);
            Vector3f min = entity.aabb.min;
            Vector3f max = entity.aabb.max;
            Vector3f[] obb = entity.aabb.OBB;
            //USE OBB INSTEAD
            Vector4f worldMin = new Vector4f();
            Vector4f worldMax = new Vector4f();
            Matrix4f.transform(model, new Vector4f(min.x, min.y, min.z, 1.0f), worldMin);
            Matrix4f.transform(model, new Vector4f(max.x, max.y, max.z, 1.0f), worldMax);
            float[] worldAABB = new float[] { worldMin.x, worldMin.y, worldMin.z, worldMax.x, worldMax.y, worldMax.z };
            System.out.println("AABB: " + worldAABB[0] + ", " + worldAABB[1] + ", " + worldAABB[2] + ", " + worldAABB[3] + ", " + worldAABB[4] + ", " + worldAABB[5]);
            entity.aabb.selected = intersects(rayOrigin, rayDirection, worldAABB);
            if (entity.aabb.selected) {
                System.out.println("Intersected with " + entity.name);
            }
        }
    }
    private boolean intersects(Vector3f origin, Vector3f direction, float[] aabb) {
        Vector3f min = new Vector3f(aabb[0], aabb[1], aabb[2]);
        Vector3f max = new Vector3f(aabb[3], aabb[4], aabb[5]);

        float tmin = (min.x - origin.x) / direction.x;
        float tmax = (max.x - origin.x) / direction.x;

        if (tmin > tmax) {
            float temp = tmin;
            tmin = tmax;
            tmax = temp;
        }

        float tymin = (min.y - origin.y) / direction.y;
        float tymax = (max.y - origin.y) / direction.y;

        if (tymin > tymax) {
            float temp = tymin;
            tymin = tymax;
            tymax = temp;
        }

        if ((tmin > tymax) || (tymin > tmax)) {
            return false;
        }

        if (tymin > tmin) {
            tmin = tymin;
        }

        if (tymax < tmax) {
            tmax = tymax;
        }

        float tzmin = (min.z - origin.z) / direction.z;
        float tzmax = (max.z - origin.z) / direction.z;

        if (tzmin > tzmax) {
            float temp = tzmin;
            tzmin = tzmax;
            tzmax = temp;
        }

        if ((tmin > tzmax) || (tzmin > tmax)) {
            return false;
        }

        if (tzmin > tmin) {
            tmin = tzmin;
        }

        if (tzmax < tmax) {
            tmax = tzmax;
        }

        if (tmin < 0 && tmax < 0) {
            return false;
        }

        System.out.println("Tmin: " + tmin + ", Tmax: " + tmax);

        return true;
    }


    private float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

}
