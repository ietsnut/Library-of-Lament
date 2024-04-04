package object;

import org.lwjgl.util.vector.Vector3f;

public class OBB extends Entity {

    public boolean selected = false;

    public Vector3f center;
    public Vector3f[] axes; // Principal axes
    public float[] extents; // Extents along each axis
    public Vector3f min;
    public Vector3f max;

    public OBB(Entity entity) {
        super(entity);
    }

    @Override
    protected void load(Object... args) {
        float[] vertices = ((Entity) args[0]).vertices;
        min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (int i = 0; i < vertices.length; i += 3) {
            Vector3f vertex = new Vector3f(vertices[i], vertices[i + 1], vertices[i + 2]);
            min.x = Math.min(min.x, vertex.x);
            min.y = Math.min(min.y, vertex.y);
            min.z = Math.min(min.z, vertex.z);
            max.x = Math.max(max.x, vertex.x);
            max.y = Math.max(max.y, vertex.y);
            max.z = Math.max(max.z, vertex.z);
        }
        center = new Vector3f((min.x + max.x) / 2, (min.y + max.y) / 2, (min.z + max.z) / 2);
        axes = new Vector3f[] { new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1) };
        extents = new float[] { (max.x - min.x) / 2, (max.y - min.y) / 2, (max.z - min.z) / 2 };
        this.vertices = new float[] {
                min.x, min.y, min.z, max.x, min.y, min.z, max.x, max.y, min.z, min.x, max.y, min.z,
                min.x, min.y, max.z, max.x, min.y, max.z, max.x, max.y, max.z, min.x, max.y, max.z,
        };
        this.indices = new int[] { // Indices to draw the wireframe (lines) of the cube
                0, 1, 1, 2, 2, 3, 3, 0, // Bottom
                4, 5, 5, 6, 6, 7, 7, 4, // Top
                0, 4, 1, 5, 2, 6, 3, 7  // Sides
        };
    }

}
