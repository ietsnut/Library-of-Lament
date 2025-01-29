package object;

import org.joml.Matrix4f;

import java.util.concurrent.atomic.AtomicReference;

public class Matrix extends AtomicReference<Matrix4f> {

    private final Matrix4f buffer1 = new Matrix4f();
    private final Matrix4f buffer2 = new Matrix4f();

    public boolean changed = true;

    public Matrix() {
        super();
        set(buffer1);
    }

    public Matrix4f buffer() {
        return (this.get() == buffer1) ? buffer2 : buffer1;
    }

    public void swap() {
        this.set(buffer());
    }

}
