package property;

import org.lwjgl.util.vector.Vector3f;

public enum Axis {
    X(1, 0, 0),
    Y(0, 1, 0),
    Z(0, 0, 1);
    public final Vector3f vector;
    Axis(int x, int y, int z) {
        this.vector = new Vector3f(x, y, z);
    }
}
