package object;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import property.Load;
import property.Transformation;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL40.*;

public abstract class Entity extends Transformation implements Load {

    public int      vao = 0;
    public int[]    vbo = new int[4];

    public int[]    indices     = new int[0];
    public byte[]   vertices    = new byte[0];
    public float[]  normals     = new float[0];
    public float[]  texCoords   = new float[0];

    public IntBuffer    indicesBuffer   = null;
    public ByteBuffer   verticesBuffer  = null;
    public FloatBuffer  normalsBuffer   = null;
    public FloatBuffer  texCoordsBuffer = null;

    public final List<Texture> textures = new ArrayList<>();

    public final String namespace;
    public final String name;

    public Collider collider;
    public final boolean collidable;

    public boolean reload = false;

    public Entity() {
        this.namespace  = null;
        this.name       = null;
        this.collidable = false;
    }

    public Entity(String namespace, String name, boolean collidable) {
        this.namespace  = namespace;
        this.name       = name;
        this.collidable = collidable;
        // TODO: check if texCoords are beyond 0-1 and make texture repeat
        textures.add(new Texture(namespace, name));
    }

    @Override
    public String toString() {
        return "<" + this.getClass().getSimpleName() + "> [" + namespace + " : " + name + "] : " + bound();
    }

    @Override
    public void preload() {
        this.vao = 0;
        this.vbo = new int[4];

        this.indices    = new int[0];
        this.vertices   = new byte[0];
        this.normals    = new float[0];
        this.texCoords  = new float[0];

        this.indicesBuffer      = null;
        this.verticesBuffer     = null;
        this.normalsBuffer      = null;
        this.texCoordsBuffer    = null;
    }

    @Override
    public boolean reload() {
        return reload;
    }

    @Override
    public void postload() {
        if (this.indices.length > 0) {
            indicesBuffer = BufferUtils.createIntBuffer(indices.length);
            indicesBuffer.put(indices);
            indicesBuffer.flip();
        }
        if (this.vertices.length > 0) {
            verticesBuffer = BufferUtils.createByteBuffer(vertices.length);
            verticesBuffer.put(vertices);
            verticesBuffer.flip();
        }
        if (this.texCoords.length > 0) {
            texCoordsBuffer = BufferUtils.createFloatBuffer(texCoords.length);
            texCoordsBuffer.position(0);
            texCoordsBuffer.put(texCoords);
            texCoordsBuffer.flip();
        }
        if (this.normals.length > 0) {
            normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
            normalsBuffer.position(0);
            normalsBuffer.put(normals);
            normalsBuffer.flip();
        }
        this.collider = collidable ? new Collider() : null;
    }

    private void buffer(int i, int l, Buffer buffer) {
        int vbo = glGenBuffers();
        switch (buffer) {
            case IntBuffer ib -> {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_STATIC_DRAW);
            }
            case FloatBuffer fb -> {
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
                glVertexAttribPointer(i, l, GL_FLOAT, false, 0, 0);
            }
            case ByteBuffer bb -> {
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, bb, GL_STATIC_DRAW);
                glVertexAttribPointer(i, l, GL_BYTE, false, 0, 0);
            }
            default -> throw new IllegalArgumentException("Unsupported buffer : " + buffer.getClass());
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        this.vbo[i] = vbo;
    }

    @Override
    public void bind() {
        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);
        if (this.indicesBuffer != null) {
            buffer(0, 0, this.indicesBuffer);
        }
        if (this.verticesBuffer != null) {
            buffer(0, this instanceof FBO ? 2 : 3, this.verticesBuffer);
        }
        if (this.texCoordsBuffer != null) {
            buffer(1, 2, this.texCoordsBuffer);
        }
        if (this.normalsBuffer != null) {
            buffer(2, 3, this.normalsBuffer);
        }
        glBindVertexArray(0);
    }

    @Override
    public void unbind() {
        glDeleteVertexArrays(vao);
        for (int vbo : vbo) {
            glDeleteBuffers(vbo);
        }
    }

    public class Collider extends Entity {

        public Vector3f min;
        public Vector3f max;
        public float size;

        public Collider() {
            super();
            queue();
        }

        @Override
        public void preload() {
            super.preload();
            this.vertices   = new byte[24];
            this.indices    = new int[24];
        }

        @Override
        public void load() {
            byte[] vertices = Entity.this.vertices;
            min = new Vector3f(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);
            max = new Vector3f(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);
            for (int i = 0; i < vertices.length; i += 3) {
                min.set(Math.min(min.x, vertices[i]), Math.min(min.y, vertices[i + 1]), Math.min(min.z, vertices[i + 2]));
                max.set(Math.max(max.x, vertices[i]), Math.max(max.y, vertices[i + 1]), Math.max(max.z, vertices[i + 2]));
            }
            size = new Vector3f(max).sub(min).length();
            this.vertices = new byte[] {
                    (byte) min.x, (byte) min.y, (byte) min.z, (byte) max.x, (byte) min.y, (byte) min.z, (byte) max.x, (byte) max.y, (byte) min.z, (byte) min.x, (byte) max.y, (byte) min.z, (byte) min.x, (byte) min.y, (byte) max.z, (byte) max.x, (byte) min.y, (byte) max.z, (byte) max.x, (byte) max.y, (byte) max.z, (byte) min.x, (byte) max.y, (byte) max.z
            };
            this.indices = new int[] {
                    0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 4, 1, 5, 2, 6, 3, 7
            };
        }

    }

    private Float collision() {
        if (collider == null) {
            return -1f;
        }
        Matrix4f inverseModelMatrix = new Matrix4f();
        Entity.this.model().invert(inverseModelMatrix);
        Vector4f rayOriginModelSpace = new Vector4f(Camera.transformation.position.x, Camera.transformation.position.y, Camera.transformation.position.z, 1.0f);
        inverseModelMatrix.transform(rayOriginModelSpace);
        Vector3f ray = new Vector3f(Camera.transformation.forward()).negate().normalize();
        Vector4f rayDirectionModelSpace = new Vector4f(ray.x, ray.y, ray.z, 0.0f);
        inverseModelMatrix.transform(rayDirectionModelSpace);
        Vector3f directionNormalized = new Vector3f(rayDirectionModelSpace.x, rayDirectionModelSpace.y, rayDirectionModelSpace.z).normalize();
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
        return collision() > 0 && distance(distance);
    }

    public float distance() {
        return new Vector3f(Camera.transformation.position).sub(this.position).length();
        //return Vector3f.sub(Camera.transformation.position, this.position, null).length();
    }

    public boolean distance(Float distance) {
        return distance() < distance;
    }

    public static Entity collides(float distance, List<Entity> entities) {
        //return the enemy with the least distance
        float min = Float.MAX_VALUE;
        Entity closest = null;
        for (Entity entity : entities) {
            if (entity.bound() && entity.collider.bound() && entity.collide(distance) && entity.distance() < min) {
                min = entity.distance();
                closest = entity;
            }
        }
        return closest;
        //return entities.stream().filter(entity -> entity.collide(distance)).min(Comparator.comparing(Entity::distance)).orElse(null);
    }

}
