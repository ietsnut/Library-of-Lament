package object;

import org.lwjgl.util.vector.Vector3f;

public class AABB extends Entity {

    public boolean selected = false;
    Vector3f min, max;
    Vector3f[] OBB = new Vector3f[8];

    public AABB(Vector3f min, Vector3f max, Vector3f[] OBB) {
        super(min, max);
        this.min = min;
        this.max = max;
        this.OBB = OBB;
    }

    @Override
    protected void load(Object... args) {
        Vector3f min = (Vector3f) args[0];
        Vector3f max = (Vector3f) args[1];
        vertices = new float[] {
                min.x, min.y, min.z, max.x, min.y, min.z, max.x, max.y, min.z, min.x, max.y, min.z,
                min.x, min.y, max.z, max.x, min.y, max.z, max.x, max.y, max.z, min.x, max.y, max.z,
        };
        indices = new int[] { // Indices to draw the wireframe (lines) of the cube
                0, 1, 1, 2, 2, 3, 3, 0, // Bottom
                4, 5, 5, 6, 6, 7, 7, 4, // Top
                0, 4, 1, 5, 2, 6, 3, 7  // Sides
        };
    }

}
