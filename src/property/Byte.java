package property;

public class Byte extends Number {

    public static final byte MAX = java.lang.Byte.MAX_VALUE;
    public static final byte MIN = java.lang.Byte.MIN_VALUE;

    byte b;
    byte scalar;

    public Byte(byte b) {
        this.b = b;
    }

    public byte get() {
        return b;
    }

    public Byte set(byte b) {
        this.b = b;
        return this;
    }

    public Byte add(byte b) {
        this.b += b;
        return this;
    }

    public Byte sub(byte b) {
        this.b -= b;
        return this;
    }

    public Byte mul(byte b) {
        this.b *= b;
        return this;
    }

    public Byte div(byte b) {
        this.b /= b;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (byte i = 7; i >= 0; i--) {
            s.append((b >>> i) & 1);
        }
        return s.toString();
    }

    @Override
    public int intValue() {
        return b;
    }

    @Override
    public long longValue() {
        return b;
    }

    @Override
    public float floatValue() {
        return b;
    }

    @Override
    public double doubleValue() {
        return b;
    }

}
