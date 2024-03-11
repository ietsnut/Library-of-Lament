package object;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.net.IDN;
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

    public static final Map<Class<?>, List<Entity>> ALL = new HashMap<>();

    public int vaoID;
    public List<Integer> vboIDs = new ArrayList<>();

    public int[] indices;
    public float[] vertices;
    public float[] normals;
    public float[] texCoords;

    public Texture texture;

    public Vector3f position = new Vector3f(0, 0, 0);
    public Vector3f rotation = new Vector3f(0, 0, 0);
    public Vector3f scale = new Vector3f(1, 1, 1);
    public int dimensions;

    public Entity(String name, int dimensions) {
        this.dimensions = dimensions;
        this.vaoID = glGenVertexArrays();
        glBindVertexArray(this.vaoID);
        load(name);
        if (this.indices != null) {
            bindIndicesBuffer(this.indices);
        }
        if (this.vertices != null) {
            bindFloatBuffer(0, dimensions, this.vertices);
        }
        if (this.texCoords != null) {
            bindFloatBuffer(1, 2, this.texCoords);
        }
        if (this.normals != null) {
            bindFloatBuffer(2, 3, this.normals);
        }
        glBindVertexArray(0);
        if (!ALL.containsKey(getClass())) {
            ALL.put(getClass(), new ArrayList<>());
        }
        ALL.get(getClass()).add(this);
    }

    public Entity(String name) {
        this(name, 3);
    }

    protected abstract void load(String name);

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

    public Entity position(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
        return this;
    }

    public Entity rotation(float x, float y, float z) {
        this.rotation = new Vector3f(x, y, z);
        return this;
    }

    public Entity scale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
        return this;
    }

    public Entity scale(float s) {
        this.scale = new Vector3f(s, s, s);
        return this;
    }

    public static final Vector3f AXIS_X = new Vector3f(1, 0, 0);
    public static final Vector3f AXIS_Y = new Vector3f(0, 1, 0);
    public static final Vector3f AXIS_Z = new Vector3f(0, 0, 1);

    public Matrix4f getModelMatrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.translate(position);
        matrix.rotate((float) Math.toRadians(rotation.x), AXIS_X);
        matrix.rotate((float) Math.toRadians(rotation.y), AXIS_Y);
        matrix.rotate((float) Math.toRadians(rotation.z), AXIS_Z);
        matrix.scale(new Vector3f(scale.x, scale.y, scale.z));
        return matrix;
    }

}
