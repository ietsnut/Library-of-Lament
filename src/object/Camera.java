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

            Matrix4f modelMatrix        = entity.transformation.model();
            Vector3f rayOrigin          = new Vector3f(transformation.position);    //in world space
            Vector3f ray                = transformation.forward();
            Vector3f rayDirection       = new Vector3f(-ray.x, -ray.y, -ray.z).normalise(null);
            OBB obb                     = entity.obb;                               //in model space

            boolean intersects = checkRayOBBIntersection(modelMatrix, rayOrigin, rayDirection, obb);
            entity.obb.selected = intersects;
            if (intersects) {
                System.out.println(entity + " selected");
            }

        }

    }

    private boolean checkRayOBBIntersection(Matrix4f modelMatrix, Vector3f rayOrigin, Vector3f rayDirection, OBB obb) {
        Matrix4f inverseModelMatrix = new Matrix4f();
        Matrix4f.invert(modelMatrix, inverseModelMatrix);
        Vector4f rayOriginModelSpace = new Vector4f(rayOrigin.x, rayOrigin.y, rayOrigin.z, 1.0f);
        Matrix4f.transform(inverseModelMatrix, rayOriginModelSpace, rayOriginModelSpace);
        Vector4f rayDirectionModelSpace = new Vector4f(rayDirection.x, rayDirection.y, rayDirection.z, 0.0f);
        Matrix4f.transform(inverseModelMatrix, rayDirectionModelSpace, rayDirectionModelSpace);
        Vector3f directionNormalized = new Vector3f(rayDirectionModelSpace.x, rayDirectionModelSpace.y, rayDirectionModelSpace.z).normalise(null);
        Vector3f invDir = new Vector3f(1.0f / directionNormalized.x, 1.0f / directionNormalized.y, 1.0f / directionNormalized.z);
        float t1 = (obb.min.x - rayOriginModelSpace.x) * invDir.x;
        float t2 = (obb.max.x - rayOriginModelSpace.x) * invDir.x;
        float t3 = (obb.min.y - rayOriginModelSpace.y) * invDir.y;
        float t4 = (obb.max.y - rayOriginModelSpace.y) * invDir.y;
        float t5 = (obb.min.z - rayOriginModelSpace.z) * invDir.z;
        float t6 = (obb.max.z - rayOriginModelSpace.z) * invDir.z;
        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        // if tmax < 0, ray (line) is intersecting AABB, but the whole AABB is behind us
        if (tmax < 0) {
            return false;
        }
        // if tmin > tmax, ray doesn't intersect AABB
        if (tmin > tmax) {
            return false;
        }
        return true;
    }

    private float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

}
