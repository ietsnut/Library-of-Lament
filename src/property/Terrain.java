package property;

import engine.Console;
import resource.Mesh;
import object.Camera;
import org.joml.Vector3f;

public class Terrain extends Entity {

    Mesh terrain;

    public Terrain(String name) {
        super(name);
        terrain = meshes[state];
    }

    public float height(float x, float z) {
        for (int t = 0; t < terrain.indices.length; t += 3) {
            int i1 = terrain.indices[t] * 3;
            int i2 = terrain.indices[t + 1] * 3;
            int i3 = terrain.indices[t + 2] * 3;
            float x1 = terrain.vertices[i1];
            float y1 = terrain.vertices[i1 + 1];
            float z1 = terrain.vertices[i1 + 2];
            float x2 = terrain.vertices[i2];
            float y2 = terrain.vertices[i2 + 1];
            float z2 = terrain.vertices[i2 + 2];
            float x3 = terrain.vertices[i3];
            float y3 = terrain.vertices[i3 + 1];
            float z3 = terrain.vertices[i3 + 2];
            float[] bary = bary(x1, z1, x2, z2, x3, z3, x, z);
            if (inside(bary)) {
                return bary[0] * y1 + bary[1] * y2 + bary[2] * y3 + 1f;
            }
        }
        return 0;
    }

    public Vector3f height(Vector3f origin, Vector3f movement) {
        Vector3f pos = new Vector3f(origin).add(movement);
        for (int t = 0; t < terrain.indices.length; t += 3) {
            int i1 = terrain.indices[t] * 3;
            int i2 = terrain.indices[t + 1] * 3;
            int i3 = terrain.indices[t + 2] * 3;
            float x1 = terrain.vertices[i1];
            float y1 = terrain.vertices[i1 + 1];
            float z1 = terrain.vertices[i1 + 2];
            float x2 = terrain.vertices[i2];
            float y2 = terrain.vertices[i2 + 1];
            float z2 = terrain.vertices[i2 + 2];
            float x3 = terrain.vertices[i3];
            float y3 = terrain.vertices[i3 + 1];
            float z3 = terrain.vertices[i3 + 2];
            if (terrain.normals[i1 + 1] < Camera.SLOPE) {
                continue;
            }
            float[] bary = bary(x1, z1, x2, z2, x3, z3, pos.x, pos.z);
            if (inside(bary)) {
                float y = bary[0] * y1 + bary[1] * y2 + bary[2] * y3 + 1f;
                if (Math.abs(y - pos.y) < 0.5f) {
                    return pos.setComponent(1, y);
                }
            }
        }
        for (int t = 0; t < terrain.indices.length; t += 3) {
            int i1 = terrain.indices[t] * 3;
            int i2 = terrain.indices[t + 1] * 3;
            int i3 = terrain.indices[t + 2] * 3;
            float x1 = terrain.vertices[i1];
            float y1 = terrain.vertices[i1 + 1];
            float z1 = terrain.vertices[i1 + 2];
            float x2 = terrain.vertices[i2];
            float y2 = terrain.vertices[i2 + 1];
            float z2 = terrain.vertices[i2 + 2];
            float x3 = terrain.vertices[i3];
            float y3 = terrain.vertices[i3 + 1];
            float z3 = terrain.vertices[i3 + 2];
            if (terrain.normals[i1 + 1] < Camera.SLOPE) {
                continue;
            }
            float[] bary = bary(x1, z1, x2, z2, x3, z3, origin.x, origin.z);
            if (inside(bary)) {
                float y = bary[0] * y1 + bary[1] * y2 + bary[2] * y3 + 1f;
                if (Math.abs(y - origin.y) < 0.1f) {
                    Vector3f movementDirection = new Vector3f(movement).normalize();
                    Vector3f edgeVector = new Vector3f();
                    if (bary[0] < 0.05f) {
                        edgeVector.set(x2 - x3, 0, z2 - z3);
                    } else if (bary[1] < 0.05f) {
                        edgeVector.set(x3 - x1, 0, z3 - z1);
                    } else if (bary[2] < 0.05f) {
                        edgeVector.set(x2 - x1, 0, z2 - z1);
                    }
                    edgeVector.normalize();
                    float dotProduct = movementDirection.dot(edgeVector);
                    Vector3f projectedMovement = new Vector3f(edgeVector).mul(dotProduct * movement.length());
                    Vector3f newPosition = new Vector3f(origin).add(projectedMovement);
                    newPosition.setComponent(1, y);
                    return newPosition;
                }
            }
        }

        return origin;
    }

    private boolean inside(float[] bary) {
        return 0 <= bary[0] && bary[0] <= 1 && 0 <= bary[1] && bary[1] <= 1 && 0 <= bary[2] && bary[2] <= 1;
    }

    private float[] bary(float x1, float z1, float x2, float z2, float x3, float z3, float x, float y) {
        float det = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 - z3);
        float l1 = ((z2 - z3) * (x - x3) + (x3 - x2) * (y - z3)) / det;
        float l2 = ((z3 - z1) * (x - x3) + (x1 - x3) * (y - z3)) / det;
        float l3 = 1.0f - l1 - l2;
        return new float[]{ l1, l2, l3 };
    }

}

