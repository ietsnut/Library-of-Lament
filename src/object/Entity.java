package object;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import property.Transformation;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public abstract class Entity {

    public static final List<Entity> ALL = new ArrayList<>();

    public int vaoID;
    public List<Integer> vboIDs = new ArrayList<>();

    public int[]    indices;
    public float[]  vertices;
    public float[]  normals;
    public float[]  texCoords;

    public List<Texture> textures = new ArrayList<>();
    public Transformation transformation = new Transformation();
    public String name;

    public Collider collider;

    public Entity(Object... args) {
        this.vaoID = glGenVertexArrays();
        glBindVertexArray(this.vaoID);
        load(args);
        if (this.indices != null) {
            bindIndicesBuffer(this.indices);
        }
        if (this.vertices != null) {
            if (args.length > 0) {
                bindFloatBuffer(0, 3, this.vertices);
            } else {
                bindFloatBuffer(0, 2, this.vertices);
            }
        }
        if (this.texCoords != null) {
            bindFloatBuffer(1, 2, this.texCoords);
        }
        if (this.normals != null) {
            bindFloatBuffer(2, 3, this.normals);
        }
        glBindVertexArray(0);
        if (args.length > 0 && args[0] instanceof String s) {
            this.name = s;
        }
        if (args.length > 0 && !(this instanceof Collider)) {
            this.collider = new Collider();
        }
        ALL.add(this);
    }

    @Override
    public String toString() {
        return name;
    }

    public Texture texture(String name) {
        Texture texture = new Texture(name);
        textures.add(texture);
        return texture;
    }

    public Texture texture() {
        Texture texture = new Texture();
        textures.add(texture);
        return texture;
    }

    protected abstract void load(Object... args);

    protected void bindIndicesBuffer(int[] indices) {
        IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
        buffer.put(indices);
        buffer.flip();
        int vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        vboIDs.add(vboId);
    }

    protected void bindFloatBuffer(int attributeNumber, int coordinateSize, float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        vboIDs.add(vboID);
    }

    public class Collider extends Entity {

        public Vector3f min;
        public Vector3f max;

        public Collider() {
            super("");
        }

        @Override
        protected void load(Object... args) {
            float[] vertices = Entity.this.vertices;
            min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
            for (int i = 0; i < vertices.length; i += 3) {
                min.set(Math.min(min.x, vertices[i]), Math.min(min.y, vertices[i + 1]), Math.min(min.z, vertices[i + 2]));
                max.set(Math.max(max.x, vertices[i]), Math.max(max.y, vertices[i + 1]), Math.max(max.z, vertices[i + 2]));
            }
            this.vertices = new float[] {
                    min.x, min.y, min.z, max.x, min.y, min.z, max.x, max.y, min.z, min.x, max.y, min.z, min.x, min.y, max.z, max.x, min.y, max.z, max.x, max.y, max.z, min.x, max.y, max.z,
            };
            this.indices = new int[] {
                    0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 4, 1, 5, 2, 6, 3, 7
            };
        }

    }

    private Float collision() {
        Matrix4f inverseModelMatrix = new Matrix4f();
        Matrix4f.invert(Entity.this.transformation.model(), inverseModelMatrix);
        Vector4f rayOriginModelSpace = new Vector4f(Camera.transformation.position.x, Camera.transformation.position.y, Camera.transformation.position.z, 1.0f);
        Matrix4f.transform(inverseModelMatrix, rayOriginModelSpace, rayOriginModelSpace);
        Vector3f ray = Camera.transformation.forward().negate(null).normalise(null);
        Vector4f rayDirectionModelSpace = new Vector4f(ray.x, ray.y, ray.z, 0.0f);
        Matrix4f.transform(inverseModelMatrix, rayDirectionModelSpace, rayDirectionModelSpace);
        Vector3f directionNormalized = new Vector3f(rayDirectionModelSpace.x, rayDirectionModelSpace.y, rayDirectionModelSpace.z).normalise(null);
        Vector3f invDir = new Vector3f(1.0f / directionNormalized.x, 1.0f / directionNormalized.y, 1.0f / directionNormalized.z);
        float t1 = (collider.min.x - rayOriginModelSpace.x) * invDir.x;
        float t2 = (collider.max.x - rayOriginModelSpace.x) * invDir.x;
        float t3 = (collider.min.y - rayOriginModelSpace.y) * invDir.y;
        float t4 = (collider.max.y - rayOriginModelSpace.y) * invDir.y;
        float t5 = (collider.min.z - rayOriginModelSpace.z) * invDir.z;
        float t6 = (collider.max.z - rayOriginModelSpace.z) * invDir.z;
        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
        // if tmax < 0, ray (line) is intersecting AABB, but the whole AABB is behind us
        if (tmax < 0) {
            return -1f; // or some indicator for no intersection
        }
        // if tmin > tmax, ray doesn't intersect AABB
        if (tmin > tmax) {
            return -1f; // or some indicator for no intersection
        }
        // if tmin < 0, the ray starts inside the AABB, so we consider the intersection distance to be 0
        return Math.max(tmin, 0);
    }

    //camera is inside AABB
    public boolean inside() {
        return collision() == 0;
    }

    //camera is outside AABB, but ray intersects AABB
    public boolean collide(float distance) {
        return collision() > 0 && distance() < distance;
    }

    public float distance() {
        return Vector3f.sub(Camera.transformation.position, this.transformation.position, null).length();
    }

    public static Entity collides(float distance, List<Entity> entities) {
        //return first entity within distance, otherwise null
        return entities.stream().filter(entity -> entity.collide(distance)).findFirst().orElse(null);
    }

}
