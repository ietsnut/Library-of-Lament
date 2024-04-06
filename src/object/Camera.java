package object;

import game.Scene;
import org.lwjgl.BufferUtils;
import property.Axis;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import property.Transformation;

import java.nio.FloatBuffer;

public class Camera {

    public static Transformation    transformation  = new Transformation();
    public static Matrix4f          view            = new Matrix4f();

    private static final float SPEED    = 0.05f;
    private static final float SENS     = 0.1f;

    public static float FOV = 75;

    public void update(Scene scene) {

        if (Mouse.isButtonDown(0)) {
            Mouse.setGrabbed(true);
        }

        if (Mouse.isButtonDown(1)) {
            Mouse.setGrabbed(false);
        }

        float dx = Mouse.getDX() * -SENS;
        float dy = Mouse.getDY() * SENS;

        if (Mouse.isGrabbed()) {
            transformation.rotate(Axis.Y, dx).rotate(Axis.X, dy).rotation(Axis.X, Math.min(Math.max(transformation.rotation.x, -80), 80));
            Vector3f forward = new Vector3f(transformation.forward().x, 0, transformation.forward().z).normalise(null);
            if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
                transformation.translate(forward.x * -SPEED, 0, forward.z * -SPEED);
            if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
                transformation.translate(forward.x * SPEED, 0, forward.z * SPEED);
            if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
                transformation.translate(forward.z * SPEED, 0, forward.x * -SPEED);
            if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT))
                transformation.translate(forward.z * -SPEED, 0, forward.x * SPEED);
            transformation.position.y = scene.terrain.getHeightOfTerrain(transformation.position.x, transformation.position.z) + 2f;
        }

        view();

    }

    public Matrix4f view() {
        transformation.norm();
        view.setIdentity();
        view.m00 = 1.0f - 2.0f * (transformation.orientation.y * transformation.orientation.y + transformation.orientation.z * transformation.orientation.z);
        view.m01 = 2.0f * (transformation.orientation.x * transformation.orientation.y - transformation.orientation.z * transformation.orientation.w);
        view.m02 = 2.0f * (transformation.orientation.x * transformation.orientation.z + transformation.orientation.y * transformation.orientation.w);
        view.m03 = 0;
        view.m10 = 2.0f * (transformation.orientation.x * transformation.orientation.y + transformation.orientation.z * transformation.orientation.w);
        view.m11 = 1.0f - 2.0f * (transformation.orientation.x * transformation.orientation.x + transformation.orientation.z * transformation.orientation.z);
        view.m12 = 2.0f * (transformation.orientation.y * transformation.orientation.z - transformation.orientation.x * transformation.orientation.w);
        view.m13 = 0;
        view.m20 = 2.0f * (transformation.orientation.x * transformation.orientation.z - transformation.orientation.y * transformation.orientation.w);
        view.m21 = 2.0f * (transformation.orientation.y * transformation.orientation.z + transformation.orientation.x * transformation.orientation.w);
        view.m22 = 1.0f - 2.0f * (transformation.orientation.x * transformation.orientation.x + transformation.orientation.y * transformation.orientation.y);
        view.m23 = 0;
        view.m30 = 0;
        view.m31 = 0;
        view.m32 = 0;
        view.m33 = 1.0f;
        view.translate(transformation.position.negate(null));
        return view;
    }

}
