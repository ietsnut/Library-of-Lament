package object;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Vector3f;
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

    public OBB obb;

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
        if (args.length > 0 && !(this instanceof OBB)) {
            this.obb = new OBB(this);
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

    public static final Vector3f AXIS_X = new Vector3f(1, 0, 0);
    public static final Vector3f AXIS_Y = new Vector3f(0, 1, 0);
    public static final Vector3f AXIS_Z = new Vector3f(0, 0, 1);


}
