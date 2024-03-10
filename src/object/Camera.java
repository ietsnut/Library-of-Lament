package object;

import engine.Renderer;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Camera {

    private final static float PI_OVER_180 = 0.0174532925f;

    public static float FOV = 70;
    public static Vector3f position = new Vector3f();
    public static Vector3f direction = new Vector3f();

    private static float pitchAngle = 0;
    private static float bearingAngle = 0;
    private static float rollAngle = 0;
    private static final Quaternion pitch = new Quaternion();;
    private static final Quaternion bearing = new Quaternion();;
    private static final Quaternion roll = new Quaternion();;
    private static final Quaternion rotation  = new Quaternion();;

    private static Vector3f rotationalVelocity = new Vector3f();
    private static final float rotationalfriction = 0.8f;

    private static float bobbingSpeed = 0.01f;
    private static final float bobbingAmount = 0.15f;
    private static double timeSinceStart = 0f;

    public static void reorient()
    {
        Quaternion.mul(roll, pitch, rotation);
        Quaternion.mul(rotation, bearing, rotation);
        Matrix4f pitchMatrix = convertQuaternionToMatrix4f(pitch);
        Quaternion temp = Quaternion.mul(bearing, pitch, null);
        Matrix4f matrix = convertQuaternionToMatrix4f(temp);
        direction.x = matrix.m20;
        direction.y = pitchMatrix.m21;
        direction.z = matrix.m22;
    }

    public static void bearing(float bearingDelta)
    {
        bearingAngle += bearingDelta;
        bearing.setFromAxisAngle(new Vector4f(0f, 1f, 0f, bearingAngle * PI_OVER_180));
    }

    public static void pitch(float pitchDelta)
    {
        pitchAngle += pitchDelta;
        pitch.setFromAxisAngle(new Vector4f(1f, 0f, 0f, pitchAngle * PI_OVER_180));
    }

    public static void roll(float rollDelta)
    {
        rollAngle += rollDelta;
        roll.setFromAxisAngle(new Vector4f(0f, 0f, 1f, rollAngle * PI_OVER_180));
    }

    public static void move(Terrain terrain) {
        if (Mouse.isButtonDown(1)) {
            Mouse.setGrabbed(false);
        }
        if (Mouse.isButtonDown(0)) {
            Mouse.setGrabbed(true);
        }
        if (!Mouse.isGrabbed()) {
            return;
        }
        rotationalVelocity.x += Mouse.getDY() / -30f;
        rotationalVelocity.y += Mouse.getDX() / 30f;
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            roll(0.5f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            roll(-0.5f);
        }
        rotationalVelocity.scale(rotationalfriction);
        bearing(rotationalVelocity.y);
        pitch(rotationalVelocity.x);
        if (Math.abs(rotationalVelocity.x) < 0.001f) rotationalVelocity.x = 0;
        if (Math.abs(rotationalVelocity.y) < 0.001f) rotationalVelocity.y = 0;
        if (Math.signum(rollAngle) == 0) {
            rollAngle = 0;
            roll.setFromAxisAngle(new Vector4f(0f, 0f, 1f, rollAngle * PI_OVER_180));
        } if (rollAngle < 0) {
            roll(0.25f);
        } else if (rollAngle > 0) {
            roll(-0.25f);
        }
        if (rollAngle > 3) {
            rollAngle = 3;
            roll.setFromAxisAngle(new Vector4f(0f, 0f, 1f, rollAngle * PI_OVER_180));
        } else if (rollAngle < -3) {
            rollAngle = -3;
            roll.setFromAxisAngle(new Vector4f(0f, 0f, 1f, rollAngle * PI_OVER_180));
        }
        reorient();
        float currentHeight = terrain.getHeightOfTerrain(position.x, position.z);
        Vector3f movementDirection = new Vector3f(0, 0, 0);
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            movementDirection.z -= direction.z / 16;
            movementDirection.x += direction.x / 16;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            movementDirection.z += direction.z / 16;
            movementDirection.x -= direction.x / 16;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            Vector3f cross = Vector3f.cross(direction, new Vector3f(0,1,0), null);
            movementDirection.z += cross.z / 16;
            movementDirection.x -= cross.x / 16;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            Vector3f cross = Vector3f.cross(direction, new Vector3f(0,1,0), null);
            movementDirection.z -= cross.z / 16;
            movementDirection.x += cross.x / 16;
        }
        Vector3f nextPosition = new Vector3f(position.x + movementDirection.x, 0, position.z + movementDirection.z);
        float nextHeight = terrain.getHeightOfTerrain(nextPosition.x, nextPosition.z);
        float heightDifference = nextHeight - currentHeight;
        float speedAdjustmentFactor = 1 - heightDifference * 16;
        position.x += movementDirection.x * speedAdjustmentFactor;
        position.z += movementDirection.z * speedAdjustmentFactor;
        float localbobbingSpeed = bobbingSpeed * speedAdjustmentFactor;
        if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D)) {
            timeSinceStart += Renderer.delta * localbobbingSpeed;
            position.y = currentHeight + 1.8f + (float) Math.sin(timeSinceStart) * bobbingAmount;
        }
    }

    private static Matrix4f convertQuaternionToMatrix4f(Quaternion q) {
        Matrix4f matrix = new Matrix4f();
        matrix.m00 = 1.0f - 2.0f * ( q.getY() * q.getY() + q.getZ() * q.getZ() );
        matrix.m01 = 2.0f * (q.getX() * q.getY() + q.getZ() * q.getW());
        matrix.m02 = 2.0f * (q.getX() * q.getZ() - q.getY() * q.getW());
        matrix.m03 = 0.0f;
        matrix.m10 = 2.0f * ( q.getX() * q.getY() - q.getZ() * q.getW() );
        matrix.m11 = 1.0f - 2.0f * ( q.getX() * q.getX() + q.getZ() * q.getZ() );
        matrix.m12 = 2.0f * (q.getZ() * q.getY() + q.getX() * q.getW() );
        matrix.m13 = 0.0f;
        matrix.m20 = 2.0f * ( q.getX() * q.getZ() + q.getY() * q.getW() );
        matrix.m21 = 2.0f * ( q.getY() * q.getZ() - q.getX() * q.getW() );
        matrix.m22 = 1.0f - 2.0f * ( q.getX() * q.getX() + q.getY() * q.getY() );
        matrix.m23 = 0.0f;
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        matrix.m33 = 1.0f;
        return matrix;
    }

    public static Matrix4f getViewMatrix() {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.setIdentity();
        Matrix4f rotMatrix = convertQuaternionToMatrix4f(rotation);
        Matrix4f.mul(rotMatrix, viewMatrix, viewMatrix);
        viewMatrix.translate(new Vector3f(-position.x, -position.y, -position.z));
        return viewMatrix;
    }
}