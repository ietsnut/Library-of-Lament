package object;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class Matrix extends AtomicReference<Matrix4f> {

    private final Matrix4f matrix1 = new Matrix4f();
    private final Matrix4f matrix2 = new Matrix4f();

    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    public Matrix() {
        super();
        set(matrix1);
    }

    public Matrix4f inactive() {
        return (this.get() == matrix1) ? matrix2 : matrix1;
    }

    public Matrix4f active() {
        return this.get();
    }

    public void swap() {
        inactive().get(buffer);
        this.set(inactive());
    }

    public FloatBuffer buffer() {
        return buffer;
    }

}
