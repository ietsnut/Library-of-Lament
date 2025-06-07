package property;

import resource.Mesh;
import object.Camera;
import org.joml.Vector3f;

public class Terrain extends Entity {

    private final Mesh terrain;

    public Terrain(String name) {
        super(name);
        terrain = meshes[state];
        this.update();
    }

    private static final int[] idx = new int[3];
    private static final float[] x = new float[3];
    private static final float[] y = new float[3];
    private static final float[] z = new float[3];
    private static final float[] bary = new float[3];

    private static final Vector3f edgeVector = new Vector3f();
    private static final Vector3f movementDirection = new Vector3f();
    private static final Vector3f projectedMovement = new Vector3f();
    private static final Vector3f newPosition = new Vector3f();
    private static final Vector3f tempPos = new Vector3f();
    private static final Vector3f v0 = new Vector3f();
    private static final Vector3f v1 = new Vector3f();
    private static final Vector3f faceNormal = new Vector3f();

    public float height(float xQuery, float zQuery) {
        for (int t = 0; t < terrain.indices.length; t += 3) {
            for (int i = 0; i < 3; i++) {
                idx[i] = terrain.indices[t + i] * 3;
                x[i] = terrain.vertices[idx[i]];
                y[i] = terrain.vertices[idx[i] + 1];
                z[i] = terrain.vertices[idx[i] + 2];
            }
            bary(x[0], z[0], x[1], z[1], x[2], z[2], xQuery, zQuery, bary);
            if (inside(bary)) {
                return bary[0] * y[0] + bary[1] * y[1] + bary[2] * y[2] + 1.7f;
            }
        }
        return 0;
    }

    public Vector3f height(Vector3f origin, Vector3f movement) {
        tempPos.set(origin).add(movement);
        for (int t = 0; t < terrain.indices.length; t += 3) {
            for (int i = 0; i < 3; i++) {
                idx[i] = terrain.indices[t + i] * 3;
                x[i] = terrain.vertices[idx[i]];
                y[i] = terrain.vertices[idx[i] + 1];
                z[i] = terrain.vertices[idx[i] + 2];
            }

            normal(x, y, z, faceNormal);
            if (faceNormal.y < Camera.SLOPE) continue;

            bary(x[0], z[0], x[1], z[1], x[2], z[2], tempPos.x, tempPos.z, bary);
            if (inside(bary)) {
                float yResult = bary[0] * y[0] + bary[1] * y[1] + bary[2] * y[2] + 1.7f;
                if (Math.abs(yResult - tempPos.y) < 0.5f) {
                    return tempPos.setComponent(1, yResult);
                }
            }
        }

        for (int t = 0; t < terrain.indices.length; t += 3) {
            for (int i = 0; i < 3; i++) {
                idx[i] = terrain.indices[t + i] * 3;
                x[i] = terrain.vertices[idx[i]];
                y[i] = terrain.vertices[idx[i] + 1];
                z[i] = terrain.vertices[idx[i] + 2];
            }

            normal(x, y, z, faceNormal);
            if (faceNormal.y < Camera.SLOPE) continue;

            bary(x[0], z[0], x[1], z[1], x[2], z[2], origin.x, origin.z, bary);
            if (inside(bary)) {
                float yResult = bary[0] * y[0] + bary[1] * y[1] + bary[2] * y[2] + 1.7f;
                if (Math.abs(yResult - origin.y) < 0.1f) {
                    movementDirection.set(movement).normalize();

                    if (bary[0] < 0.05f) {
                        edgeVector.set(x[1] - x[2], 0, z[1] - z[2]);
                    } else if (bary[1] < 0.05f) {
                        edgeVector.set(x[2] - x[0], 0, z[2] - z[0]);
                    } else if (bary[2] < 0.05f) {
                        edgeVector.set(x[1] - x[0], 0, z[1] - z[0]);
                    }
                    edgeVector.normalize();

                    float dot = movementDirection.dot(edgeVector);
                    projectedMovement.set(edgeVector).mul(dot * movement.length());
                    newPosition.set(origin).add(projectedMovement);
                    newPosition.setComponent(1, yResult);
                    return newPosition;
                }
            }
        }

        return origin;
    }

    private void normal(float[] x, float[] y, float[] z, Vector3f out) {
        v0.set(x[1] - x[0], y[1] - y[0], z[1] - z[0]);
        v1.set(x[2] - x[0], y[2] - y[0], z[2] - z[0]);
        v0.cross(v1, out).normalize();
    }

    private boolean inside(float[] bary) {
        return 0 <= bary[0] && bary[0] <= 1 &&
                0 <= bary[1] && bary[1] <= 1 &&
                0 <= bary[2] && bary[2] <= 1;
    }

    private void bary(float x1, float z1, float x2, float z2, float x3, float z3, float x, float z, float[] out) {
        float det = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 - z3);
        float l1 = ((z2 - z3) * (x - x3) + (x3 - x2) * (z - z3)) / det;
        float l2 = ((z3 - z1) * (x - x3) + (x1 - x3) * (z - z3)) / det;
        float l3 = 1.0f - l1 - l2;
        out[0] = l1;
        out[1] = l2;
        out[2] = l3;
    }
}
