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

    public float move(Vector3f newPlayerWorldPosition) {
        for (int i = 0; i < vertices.length; i += 9) {
            if (normals[i + 1] < 0.7071) {
                continue;
            }
            Vector3f v1 = new Vector3f(vertices[i], vertices[i + 1], vertices[i + 2]);
            Vector3f v2 = new Vector3f(vertices[i + 3], vertices[i + 4], vertices[i + 5]);
            Vector3f v3 = new Vector3f(vertices[i + 6], vertices[i + 7], vertices[i + 8]);
            if (pointInXZTriangle(newPlayerWorldPosition, v1, v2, v3)) {
                System.out.println("triangle example: " + v1 + ", " + v2 + ", " + v3);
                return barryCentric(v1, v2, v3, new Vector2f(newPlayerWorldPosition.x, newPlayerWorldPosition.z));
            }
        }
        return -1;
    }

    private boolean pointInXZTriangle(Vector3f pt, Vector3f v1, Vector3f v2, Vector3f v3) {
        float denominator = (v2.z - v3.z) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.z - v3.z);
        float a = ((v2.z - v3.z) * (pt.x - v3.x) + (v3.x - v2.x) * (pt.z - v3.z)) / denominator;
        float b = ((v3.z - v1.z) * (pt.x - v3.x) + (v1.x - v3.x) * (pt.z - v3.z)) / denominator;
        float c = 1 - a - b;
        return (0 <= a && a <= 1) && (0 <= b && b <= 1) && (0 <= c && c <= 1);
    }

    private float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    /*
    public float move(Vector3f newPlayerWorldPosition) {

        // the vertices are already in world space, so we can use them directly
        // the vertices are bytes because they are in the range -128 to 127, you can use them directly as floats
        byte[] vertices = this.vertices;
        float[] normals = this.normals;

        //TODO: go through the vertexes and their normals and find the triangle the player is on.
        // the playerworldposition y is always at its feet (0)
        // the vertices are already in world space, so we can use them directly
        // Look if the position is on a horizontal plane (normal up) or slope of max 45 degrees, otherwise return -1
        // use the normals array to look up the corresponding normal for the vertex
        // return the height of the terrain at the player position
        // you can use JOML
        // also make sure that vertices above the player are not considered (since there could be a ceiling above the player)

        System.out.println("position example: " + newPlayerWorldPosition);
        System.out.println("triangle example: " + vertices[4] + ", " + vertices[2] + ", " + vertices[3]);
        System.out.println("normal example: " + normals[1] + ", " + normals[2] + ", " + normals[3]);

        return -1;


    }*/

    @Override
    public void load() {
        obj("terrain");
    }

    @Override
    public Matrix4f model() {
        return this.model.identity().translate(position).scale(scale);
    }

}

