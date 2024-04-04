package property;

import org.lwjgl.util.vector.*;

public class Transformation {

    public final Vector4f orientation   = new Vector4f(0, 0, 0, 1);
    public final Vector3f rotation      = new Vector3f(0, 0, 0);
    public final Vector3f position      = new Vector3f(0, 0, 0);
    public final Vector3f scale         = new Vector3f(1, 1, 1);

    private final Matrix4f view         = new Matrix4f();
    private final Matrix4f model        = new Matrix4f();

    public Transformation position(Vector3f position) {
        this.position.set(position);
        return this;
    }

    public Transformation position(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    public Transformation position(Axis axis, float p) {
        switch (axis) {
            case X -> position.x = p;
            case Y -> position.y = p;
            case Z -> position.z = p;
        }
        return this;
    }

    public Transformation translate(Vector3f translation) {
        return translate(translation.x, translation.y, translation.z);
    }

    public Transformation translate(float x, float y, float z) {
        position.translate(x, y, z);
        return this;
    }

    public Transformation translate(Axis axis, float p) {
        position.x += (axis == Axis.X) ? p : 0;
        position.y += (axis == Axis.Y) ? p : 0;
        position.z += (axis == Axis.Z) ? p : 0;
        return this;
    }

    public Transformation scale(float s) {
        scale.set(s, s, s);
        return this;
    }

    public Transformation scale(Transformation transformation) {
        return scale(transformation.scale);
    }

    public Transformation scale(Vector3f scale) {
        this.scale.set(scale);
        return this;
    }

    public Transformation scale(float x, float y, float z) {
        scale.set(x, y, z);
        return this;
    }

    public Transformation scale(Axis axis, float s) {
        switch (axis) {
            case X -> scale.x = s;
            case Y -> scale.y = s;
            case Z -> scale.z = s;
        }
        return this;
    }

    public Transformation flip(Axis axis) {
        switch (axis) {
            case X -> scale.x *= -1;
            case Y -> scale.y *= -1;
            case Z -> scale.z *= -1;
        }
        return this;
    }

    public Transformation rotation(Transformation transformation) {
        return rotation(transformation.rotation);
    }

    public Transformation rotation(Vector3f rotation) {
        this.rotation.set(rotation);
        return orient();
    }

    public Transformation rotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
        return orient();
    }

    public Transformation rotation(Axis axis, float r) {
        switch (axis) {
            case X -> this.rotation.x = r;
            case Y -> this.rotation.y = r;
            case Z -> this.rotation.z = r;
        }
        return orient();
    }

    public Transformation rotate(Axis axis, float r) {
        this.rotation.x += (axis == Axis.X) ? r : 0;
        this.rotation.y += (axis == Axis.Y) ? r : 0;
        this.rotation.z += (axis == Axis.Z) ? r : 0;
        return orient();
    }

    public Transformation rotate(float x, float y, float z) {
        this.rotation.x += x;
        this.rotation.y += y;
        this.rotation.z += z;
        return orient();
    }

    private Transformation rot(Axis axis, double angle) {
        float sinHalfAngle = (float) Math.sin(angle / 2);
        float rx = (axis == Axis.X) ? sinHalfAngle : 0;
        float ry = (axis == Axis.Y) ? sinHalfAngle : 0;
        float rz = (axis == Axis.Z) ? sinHalfAngle : 0;
        mul(rx, ry, rz, (float) Math.cos(angle / 2));
        return this;
    }

    public Transformation orient() {
        orientation.set(0, 0, 0, 1);
        return rot(Axis.Y, Math.toRadians(rotation.y)).rot(Axis.X, Math.toRadians(rotation.x)).rot(Axis.Z, Math.toRadians(rotation.z)).norm();
    }

    private void mul(float dx, float dy, float dz, float dw) {
        float newX = orientation.w * dx + orientation.x * dw + orientation.y * dz - orientation.z * dy;
        float newY = orientation.w * dy - orientation.x * dz + orientation.y * dw + orientation.z * dx;
        float newZ = orientation.w * dz + orientation.x * dy - orientation.y * dx + orientation.z * dw;
        float newW = orientation.w * dw - orientation.x * dx - orientation.y * dy - orientation.z * dz;
        orientation.set(newX, newY, newZ, newW);
    }

    public Transformation norm() {
        float norm = (float) Math.sqrt(orientation.x * orientation.x + orientation.y * orientation.y + orientation.z * orientation.z + orientation.w * orientation.w);
        orientation.x /= norm;
        orientation.y /= norm;
        orientation.z /= norm;
        orientation.w /= norm;
        return this;
    }

    public Matrix4f view() {
        norm();
        view.setIdentity();
        view.m00 = 1.0f - 2.0f * (orientation.y * orientation.y + orientation.z * orientation.z);
        view.m01 = 2.0f * (orientation.x * orientation.y - orientation.z * orientation.w);
        view.m02 = 2.0f * (orientation.x * orientation.z + orientation.y * orientation.w);
        view.m03 = 0;
        view.m10 = 2.0f * (orientation.x * orientation.y + orientation.z * orientation.w);
        view.m11 = 1.0f - 2.0f * (orientation.x * orientation.x + orientation.z * orientation.z);
        view.m12 = 2.0f * (orientation.y * orientation.z - orientation.x * orientation.w);
        view.m13 = 0;
        view.m20 = 2.0f * (orientation.x * orientation.z - orientation.y * orientation.w);
        view.m21 = 2.0f * (orientation.y * orientation.z + orientation.x * orientation.w);
        view.m22 = 1.0f - 2.0f * (orientation.x * orientation.x + orientation.y * orientation.y);
        view.m23 = 0;
        view.m30 = 0;
        view.m31 = 0;
        view.m32 = 0;
        view.m33 = 1.0f;
        view.translate(new Vector3f(-position.x, -position.y, -position.z));
        return view;
    }


    public Matrix4f model() {
        model.setIdentity();
        Matrix4f.translate(position, model, model);
        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.setIdentity();
        float xy = orientation.x * orientation.y;
        float xz = orientation.x * orientation.z;
        float xw = orientation.x * orientation.w;
        float yz = orientation.y * orientation.z;
        float yw = orientation.y * orientation.w;
        float zw = orientation.z * orientation.w;
        float xSquared = orientation.x * orientation.x;
        float ySquared = orientation.y * orientation.y;
        float zSquared = orientation.z * orientation.z;
        rotationMatrix.m00 = 1 - 2 * (ySquared + zSquared);
        rotationMatrix.m01 = 2 * (xy - zw);
        rotationMatrix.m02 = 2 * (xz + yw);
        rotationMatrix.m10 = 2 * (xy + zw);
        rotationMatrix.m11 = 1 - 2 * (xSquared + zSquared);
        rotationMatrix.m12 = 2 * (yz - xw);
        rotationMatrix.m20 = 2 * (xz - yw);
        rotationMatrix.m21 = 2 * (yz + xw);
        rotationMatrix.m22 = 1 - 2 * (xSquared + ySquared);
        Matrix4f.mul(model, rotationMatrix, model);
        Matrix4f.scale(new Vector3f(scale.x, scale.y, scale.z), model, model);
        return model;
    }

    /*
    public Matrix4f model() {
        model.setIdentity();
        model.translate(position);
        model.rotate((float) Math.toRadians(rotation.x), Axis.X.vector);
        model.rotate((float) Math.toRadians(rotation.y), Axis.Y.vector);
        model.rotate((float) Math.toRadians(rotation.z), Axis.Z.vector);
        model.scale(new Vector3f(scale.x, scale.y, scale.z));
        return model;
    }*/

    public Vector3f forward() {
        norm();
        return new Vector3f(2.0f * (orientation.x * orientation.z + orientation.w * orientation.y), 2.0f * (orientation.y * orientation.z - orientation.w * orientation.x), 1.0f - 2.0f * (orientation.x * orientation.x + orientation.y * orientation.y));
    }

}
