package property;

import resource.Mesh;

public class Cylinder extends Mesh {

    private final int sides;
    private final float radius;
    private float height;

    public Cylinder(int sides, float radius) {
        this.sides = sides;
        this.radius = radius;
    }

    @Override
    public void load() {

        float angleStep = (float) (2.0 * Math.PI / sides);
        float chord = 2.0f * radius * (float) Math.sin(angleStep / 2.0);

        this.height = chord;

        float halfHeight = 0.5f * height;
        int totalVertices = sides * 4;
        int totalIndices  = sides * 6;

        this.vertices   = new byte[totalVertices * 3];
        this.normals    = new float[totalVertices * 3];
        this.uvs        = new float[totalVertices * 2];
        this.indices    = new int[totalIndices];

        for (int i = 0; i < sides; i++) {
            float angle0 = i * angleStep;
            float angle1 = (i + 1) * angleStep;

            float x0 = (float) (Math.cos(angle0) * radius);
            float z0 = (float) (Math.sin(angle0) * radius);
            float x1 = (float) (Math.cos(angle1) * radius);
            float z1 = (float) (Math.sin(angle1) * radius);

            int baseVert = i * 4;
            int vx = baseVert * 3;
            int tx = baseVert * 2;

            this.vertices[vx]     = (byte) x0;
            this.vertices[vx + 1] = (byte) -halfHeight;
            this.vertices[vx + 2] = (byte) z0;
            this.vertices[vx + 3] = (byte) x1;
            this.vertices[vx + 4] = (byte) -halfHeight;
            this.vertices[vx + 5] = (byte) z1;
            this.vertices[vx + 6]  = (byte) x1;
            this.vertices[vx + 7]  = (byte) halfHeight;
            this.vertices[vx + 8]  = (byte) z1;
            this.vertices[vx + 9]  = (byte) x0;
            this.vertices[vx + 10] = (byte) halfHeight;
            this.vertices[vx + 11] = (byte) z0;

            float midAngle = angle0 + 0.5f * angleStep;
            float nx = (float) Math.cos(midAngle);
            float nz = (float) Math.sin(midAngle);

            this.normals[vx]     = nx; this.normals[vx + 1]  = 0.0f; this.normals[vx + 2]  = nz;
            this.normals[vx + 3] = nx; this.normals[vx + 4]  = 0.0f; this.normals[vx + 5]  = nz;
            this.normals[vx + 6] = nx; this.normals[vx + 7]  = 0.0f; this.normals[vx + 8]  = nz;
            this.normals[vx + 9] = nx; this.normals[vx + 10] = 0.0f; this.normals[vx + 11] = nz;

            this.uvs[tx]     = 0.0f;  this.uvs[tx + 1]  = 0.0f;  // bottom-left
            this.uvs[tx + 2] = 1.0f;  this.uvs[tx + 3]  = 0.0f;  // bottom-right
            this.uvs[tx + 4] = 1.0f;  this.uvs[tx + 5]  = 1.0f;  // top-right
            this.uvs[tx + 6] = 0.0f;  this.uvs[tx + 7]  = 1.0f;  // top-left

            int baseIdx = i * 6;
            this.indices[baseIdx]     = baseVert;
            this.indices[baseIdx + 1] = baseVert + 1;
            this.indices[baseIdx + 2] = baseVert + 2;
            this.indices[baseIdx + 3] = baseVert + 2;
            this.indices[baseIdx + 4] = baseVert + 3;
            this.indices[baseIdx + 5] = baseVert;
        }
    }

    @Override
    public void unload() {
        indices     = null;
        vertices    = null;
        uvs         = null;
        normals     = null;
    }

}
