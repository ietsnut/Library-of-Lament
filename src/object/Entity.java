package object;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import property.Load;
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

public abstract class Entity extends Transformation implements Load {

    public int vao;
    public List<Integer> vbo = new ArrayList<>();

    public int[]    indices;
    public float[]  vertices;
    public float[]  normals;
    public float[]  texCoords;

    public IntBuffer    indicesBuffer;
    public FloatBuffer  verticesBuffer;
    public FloatBuffer  normalsBuffer;
    public FloatBuffer  texCoordsBuffer;

    public final List<Texture> textures = new ArrayList<>();

    public final int dimensions;
    public final String name;
    public Collider collider;
    public final boolean collidable;

    public Entity(String name, boolean collidable) {
        this.name = name;
        this.dimensions = this instanceof FBO ? 2 : 3;
        this.collidable = collidable;
    }

    public Entity texture(Texture texture) {
        textures.add(texture);
        return this;
    }

    @Override
    public String toString() {
        return "<" + this.getClass().getSimpleName() + "> [" + name + "] : " + position.x + ", " + position.y + ", " + position.z;
    }

    protected void bindBuffer(int attributeNumber, int coordinateSize, FloatBuffer buffer) {
        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        vbo.add(vboID);
    }

    @Override
    public void bind() {
        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);
        if (this.indicesBuffer != null) {
            int vboId = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
            vbo.add(vboId);
        }
        if (this.verticesBuffer != null) {
            bindBuffer(0, this.dimensions, this.verticesBuffer);
        }
        if (this.texCoordsBuffer != null) {
            bindBuffer(1, 2, this.texCoordsBuffer);
        }
        if (this.normalsBuffer != null) {
            bindBuffer(2, 3, this.normalsBuffer);
        }
        glBindVertexArray(0);
        this.collider = collidable ? new Collider() : null;
    }

    @Override
    public void unload() {
        glDeleteVertexArrays(vao);
        for (int vbo : vbo) {
            glDeleteBuffers(vbo);
        }
    }

    @Override
    public void run() {
        Load.super.run();
        post();
    }

    public void post() {
        if (this.indices != null) {
            indicesBuffer = BufferUtils.createIntBuffer(indices.length);
            indicesBuffer.put(indices);
            indicesBuffer.flip();
        }
        if (this.vertices != null) {
            verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
            verticesBuffer.put(vertices);
            verticesBuffer.flip();
        }
        if (this.texCoords != null) {
            texCoordsBuffer = BufferUtils.createFloatBuffer(texCoords.length);
            texCoordsBuffer.put(texCoords);
            texCoordsBuffer.flip();
        }
        if (this.normals != null) {
            normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
            normalsBuffer.put(normals);
            normalsBuffer.flip();
        }
    }

    public class Collider extends Entity {

        public Vector3f min;
        public Vector3f max;
        public float size;

        public Collider() {
            super("collider", false);
            load();
            post();
            bind();
            BOUND.add(this);
        }

        @Override
        public void load() {
            float[] vertices = Entity.this.vertices;
            min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
            for (int i = 0; i < vertices.length; i += 3) {
                min.set(Math.min(min.x, vertices[i]), Math.min(min.y, vertices[i + 1]), Math.min(min.z, vertices[i + 2]));
                max.set(Math.max(max.x, vertices[i]), Math.max(max.y, vertices[i + 1]), Math.max(max.z, vertices[i + 2]));
            }
            size = new Vector3f(max).sub(min).length();
            this.vertices = new float[] {
                    min.x, min.y, min.z, max.x, min.y, min.z, max.x, max.y, min.z, min.x, max.y, min.z, min.x, min.y, max.z, max.x, min.y, max.z, max.x, max.y, max.z, min.x, max.y, max.z,
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
            if (entity.collide(distance) && entity.distance() < min) {
                min = entity.distance();
                closest = entity;
            }
        }
        return closest;
        //return entities.stream().filter(entity -> entity.collide(distance)).min(Comparator.comparing(Entity::distance)).orElse(null);
    }

}
