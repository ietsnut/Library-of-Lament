package property;

import java.io.Serializable;

public class Vector implements Cloneable, Serializable {

    public byte x, y, z;

    public Vector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector(byte x, byte y, byte z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(Vector vector) {
        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
    }

    public Vector add(byte x, byte y, byte z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector setComponent(byte index, byte value) {
        switch (index) {
            case 0:
                x = value;
                break;
            case 1:
                y = value;
                break;
            case 2:
                z = value;
                break;
        }
        return this;
    }

    public byte getComponent(byte index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> 0;
        };
    }

    public Vector add(Vector vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
        return this;
    }

    public Vector sub(byte x, byte y, byte z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector sub(Vector vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        this.z -= vector.z;
        return this;
    }

    public Vector mul(byte x, byte y, byte z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vector mul(Vector vector) {
        this.x *= vector.x;
        this.y *= vector.y;
        this.z *= vector.z;
        return this;
    }

    public Vector div(byte x, byte y, byte z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public Vector div(Vector vector) {
        this.x /= vector.x;
        this.y /= vector.y;
        this.z /= vector.z;
        return this;
    }

    public Vector set(byte x, byte y, byte z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector set(Vector vector) {
        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
        return this;
    }

    public Vector negate() {
        x = (byte) -x;
        y = (byte) -y;
        z = (byte) -z;
        return this;
    }

    public byte distance(Vector vector) {
        byte dx = (byte) (x - vector.x);
        byte dy = (byte) (y - vector.y);
        byte dz = (byte) (z - vector.z);
        return (byte) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public byte length() {
        return (byte) Math.sqrt(x * x + y * y + z * z);
    }

    public byte dot(Vector vector) {
        return (byte) (x * vector.x + y * vector.y + z * vector.z);
    }

    public Vector cross(Vector vector) {
        byte x = (byte) (this.y * vector.z - this.z * vector.y);
        byte y = (byte) (this.z * vector.x - this.x * vector.z);
        byte z = (byte) (this.x * vector.y - this.y * vector.x);
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector normalize() {
        byte length = length();
        if (length != 0) {
            x /= length;
            y /= length;
            z /= length;
        }
        return this;
    }

    @Override
    public Vector clone() {
        try {
            Vector clone = (Vector) super.clone();
            clone.x = x;
            clone.y = y;
            clone.z = z;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
