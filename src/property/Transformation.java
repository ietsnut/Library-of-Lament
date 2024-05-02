package property;

import org.joml.*;
import org.joml.Math;

public class Transformation {

    public static final float UNIT  = 1f;
    public static final float DECI  = 1f / 2;
    public static final float CENTI = 1f / 4;
    public static final float MILLI = 1f / 8;
    public static final float MICRO = 1f / 16;
    public static final float NANO  = 1f / 32;
    public static final float PICO  = 1f / 64;
    public static final float FEMTO = 1f / 128;
    public static final float ATTO  = 1f / 256;

    public final Quaternionf    orientation = new Quaternionf();
    public final Vector3f       rotation    = new Vector3f(0, 0, 0);
    public final Vector3f       position    = new Vector3f(0, 0, 0);
    public float                scale       = UNIT;
    public final Matrix4f       model       = new Matrix4f();

    public Transformation(float x, float y, float z) {
        position.set(x, y, z);
    }

    public Transformation(float scale) {
        this.scale = scale;
    }

    public Transformation() {}

    private Transformation rot(float x, float y, float z, double angle) {
        Quaternionf deltaRotation = new Quaternionf().rotationAxis((float) angle, x, y, z);
        orientation.mul(deltaRotation);
        return this;
    }

    public Transformation orient() {
        orientation.identity();
        rot(0, 1, 0, Math.toRadians(rotation.y));
        rot(1, 0, 0, Math.toRadians(rotation.x));
        rot(0, 0, 1, Math.toRadians(rotation.z));
        return this;
    }

    protected Matrix4f model() {
        orient();
        return new Matrix4f().identity().translate(position).rotate(orientation).scale(scale);
    }

    public final void remodel() {
        this.model.set(model());
    }

    public Vector3f forward() {
        return orientation.transform(new Vector3f(0, 0, 1));
    }

}
