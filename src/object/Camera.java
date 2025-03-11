package object;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Camera {

    public static final Matrix4f VIEW_PROJECTION;
    public static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(16);;

    static {

        Vector3f cameraPos    = new Vector3f(128f, 128f, 500f); // Positioned in front, centered on your 255-sized model
        Vector3f cameraTarget = new Vector3f(0, 0, 0);          // Looking towards the center of your scene
        Vector3f upDirection  = new Vector3f(0, 1f, 0);         // Y-axis is "up"

        Matrix4f VIEW = new Matrix4f().lookAt(cameraPos, cameraTarget, upDirection);

        float halfWidth  = 127.5f;
        float halfHeight = 127.5f;
        float padding    = 0;
        float left   = -halfWidth - padding;
        float right  = halfWidth + padding;
        float bottom = -halfHeight - padding;
        float top    = halfHeight + padding;
        float near = 500f;
        float far  = 600f;

        Matrix4f PROJECTION = new Matrix4f().ortho(left, right, bottom, top, near, far);

        VIEW_PROJECTION = new Matrix4f(PROJECTION).mul(VIEW);

        VIEW_PROJECTION.get(BUFFER);

    }
}
