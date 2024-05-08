package content;

import object.Camera;
import object.Entity;
import org.joml.Vector3f;
import property.Mesh;

public class Terrain extends Entity {

    public Mesh terrain;

    public Terrain(byte id) {
        super(id);
        terrain = meshes.getFirst();
    }

    public float height(float x, float z) {
        for (int i = 0; i < terrain.vertices.length; i += 9) {
            float[] bary = bary(terrain.vertices[i], terrain.vertices[i + 2], terrain.vertices[i + 3], terrain.vertices[i + 5], terrain.vertices[i + 6], terrain.vertices[i + 8], x, z);
            if (inside(bary)) {
                return bary[0] * terrain.vertices[i + 1] + bary[1] * terrain.vertices[i + 4] + bary[2] * terrain.vertices[i + 7] + 1f;
            }
        }
        return 0;
    }

    public Vector3f height(Vector3f origin, Vector3f movement) {
        Vector3f pos = new Vector3f(origin).add(movement);
        for (int i = 0; i < terrain.vertices.length; i += 9) {
            if (terrain.normals[i + 1] < Camera.SLOPE) {
                continue;
            }
            float[] bary = bary(terrain.vertices[i], terrain.vertices[i + 2], terrain.vertices[i + 3], terrain.vertices[i + 5], terrain.vertices[i + 6], terrain.vertices[i + 8], pos.x, pos.z);
            if (inside(bary)) {
                float y = bary[0] * terrain.vertices[i + 1] + bary[1] * terrain.vertices[i + 4] + bary[2] * terrain.vertices[i + 7] + 1f;
                if (Math.abs(y - pos.y) < 0.5f) {
                    return pos.setComponent(1, y);
                }
            }
        }
        for (int i = 0; i < terrain.vertices.length; i += 9) {
            if (terrain.normals[i + 1] < Camera.SLOPE) {
                continue;
            }
            float[] bary = bary(terrain.vertices[i], terrain.vertices[i + 2], terrain.vertices[i + 3], terrain.vertices[i + 5], terrain.vertices[i + 6], terrain.vertices[i + 8], origin.x, origin.z);
            if (inside(bary)) {
                float y = bary[0] * terrain.vertices[i + 1] + bary[1] * terrain.vertices[i + 4] + bary[2] * terrain.vertices[i + 7] + 1f;
                if (Math.abs(y - origin.y) < 0.1f) {
                    Vector3f movementDirection = new Vector3f(movement).normalize();
                    Vector3f edgeVector = new Vector3f();
                    if (bary[0] < 0.05f) {
                        edgeVector.set(terrain.vertices[i + 3] - terrain.vertices[i + 6], 0, terrain.vertices[i + 5] - terrain.vertices[i + 8]);
                    } else if (bary[1] < 0.05f) {
                        edgeVector.set(terrain.vertices[i + 6] - terrain.vertices[i], 0, terrain.vertices[i + 8] - terrain.vertices[i + 2]);
                    } else if (bary[2] < 0.05f) {
                        edgeVector.set(terrain.vertices[i + 3] - terrain.vertices[i], 0, terrain.vertices[i + 5] - terrain.vertices[i + 2]);
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

    private float[] bary(byte x1, byte z1, byte x2, byte z2, byte x3, byte z3, float x, float y) {
        float det = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 - z3);
        float l1 = ((z2 - z3) * (x - x3) + (x3 - x2) * (y - z3)) / det;
        float l2 = ((z3 - z1) * (x - x3) + (x1 - x3) * (y - z3)) / det;
        float l3 = 1.0f - l1 - l2;
        return new float[]{ l1, l2, l3 };
    }

    @Override
    public void update() {

    }

}

