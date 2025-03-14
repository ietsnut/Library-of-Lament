package object;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Camera {

    public static final Matrix4f VIEW_PROJECTION;
    public static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(16);;

    public static final Vector3f POSITION;
    public static final Vector3f TARGET;
    public static final Vector3f UP;

    static {

        POSITION = new Vector3f(0, 0, 256f);
        TARGET = new Vector3f(0, 0, 0);
        UP = new Vector3f(0, 1f, 0);

        Matrix4f VIEW = new Matrix4f().lookAt(POSITION, TARGET, UP);

        float hWidth    = 127.5f;
        float hHeight   = 127.5f;
        float padding   = 0;
        float left      = -hWidth - padding;
        float right     = hWidth + padding;
        float bottom    = -hHeight - padding;
        float top       = hHeight + padding;
        float near      = 127.5f;
        float far       = 400f;

        Matrix4f PROJECTION = new Matrix4f().ortho(left, right, bottom, top, near, far);

        VIEW_PROJECTION = new Matrix4f(PROJECTION).mul(VIEW);

        VIEW_PROJECTION.get(BUFFER);

    }
}
