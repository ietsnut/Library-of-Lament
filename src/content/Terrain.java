package content;

import object.Entity;
import object.Model;
import object.Texture;
import org.joml.*;
import org.joml.Math;

import java.nio.ByteBuffer;

public class Terrain extends Model {

    public Terrain(String name) {
        super(name);
        texture(new Texture("terrain", name));
        enqueue();
    }

    public float move(Vector3f origin, Vector3f movement) {
        Vector3f pos = new Vector3f(origin).add(movement);
        for (int i = 0; i < vertices.length; i += 9) {
            if (normals[i + 1] < 0.7071) {
                continue;
            }
            float[] barry = barry(vertices[i], vertices[i + 2], vertices[i + 3], vertices[i + 5], vertices[i + 6], vertices[i + 8], pos.x, pos.z);
            if ((0 <= barry[0] && barry[0] <= 1) && (0 <= barry[1] && barry[1] <= 1) && (0 <= barry[2] && barry[2] <= 1)) {
                return barry[0] * vertices[i + 1] + barry[1] * vertices[i + 4] + barry[2] * vertices[i + 7];
            }
        }
        return -1;
    }

    private float[] barry(byte x1, byte z1, byte x2, byte z2, byte x3, byte z3, float x, float y) {
        float det = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 - z3);
        float l1 = ((z2 - z3) * (x - x3) + (x3 - x2) * (y - z3)) / det;
        float l2 = ((z3 - z1) * (x - x3) + (x1 - x3) * (y - z3)) / det;
        float l3 = 1.0f - l1 - l2;
        return new float[]{ l1, l2, l3 };
    }

    @Override
    public void load() {
        obj("terrain");
    }

    @Override
    public Matrix4f model() {
        return this.model.identity().translate(position).scale(scale);
    }

}

