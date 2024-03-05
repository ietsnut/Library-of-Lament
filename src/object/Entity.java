package object;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Vector3f;

import java.net.IDN;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public abstract class Entity {

    public static final ArrayList<Entity> ALL = new ArrayList<>();

    public int ID;
    public int vaoID;
    public List<Integer> vboIDs = new ArrayList<Integer>();

    public int[] indices;
    public float[] vertices;
    public float[] normals;
    public float[] texCoords;

    public Entity(String name) {
        this.ID = ALL.size();
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
        ALL.add(this);
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

}
