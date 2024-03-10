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

    public static final Map<Class<?>, List<Entity>> ALL = new HashMap<Class<?>, List<Entity>>();

    public int vaoID;
    public List<Integer> vboIDs = new ArrayList<Integer>();

    public int[] indices;
    public float[] vertices;
    public float[] normals;
    public float[] texCoords;

    public Vector3f position = new Vector3f(0, 0, 0);
    public Vector3f rotation = new Vector3f(0, 0, 0);
    public Vector3f scale = new Vector3f(1, 1, 1);

    public Entity(String name) {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);
        load(name);
        if (indices != null) {
            bindIndicesBuffer(indices);
        }
        if (vertices != null) {
            bindFloatBuffer(0, 3, vertices);
        }
        if (texCoords != null) {
            bindFloatBuffer(1, 2, texCoords);
        }
        if (normals != null) {
            bindFloatBuffer(2, 3, normals);
        }
        glBindVertexArray(0);
        if (!ALL.containsKey(getClass())) {
            ALL.put(getClass(), new ArrayList<>());
        }
        ALL.get(getClass()).add(this);
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

    public static void clean() {
        for (List<Entity> entities : Entity.ALL.values()) {
            for (Entity entity : entities) {
                glDeleteVertexArrays(entity.vaoID);
                for (int vbo : entity.vboIDs) {
                    glDeleteBuffers(vbo);
                }
                if (entity instanceof Sky sky) {
                    glDeleteTextures(sky.texID);
                }
            }
        }
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
