package object;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import property.Transformation;

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
    public AABB aabb;

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
        if (!(this instanceof AABB) && !(this instanceof FBO)) {
            aabb = new AABB(getAABBMin(), getAABBMax(), generateOBB());
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

    public Vector3f getAABBMin() {
        Vector3f min = new Vector3f();
        for (int i = 0; i < vertices.length; i += 3) {
            min.x = Math.min(min.x, vertices[i]);
            min.y = Math.min(min.y, vertices[i + 1]);
            min.z = Math.min(min.z, vertices[i + 2]);
        }
        return min;
    }

    public Vector3f getAABBMax() {
        Vector3f max = new Vector3f();
        for (int i = 0; i < vertices.length; i += 3) {
            max.x = Math.max(max.x, vertices[i]);
            max.y = Math.max(max.y, vertices[i + 1]);
            max.z = Math.max(max.z, vertices[i + 2]);
        }
        return max;
    }

    public Vector3f[] generateOBB() {
        Vector3f[] obb = new Vector3f[8];
        Vector3f min = getAABBMin();
        Vector3f max = getAABBMax();
        obb[0] = new Vector3f(min.x, min.y, min.z);
        obb[1] = new Vector3f(max.x, min.y, min.z);
        obb[2] = new Vector3f(max.x, max.y, min.z);
        obb[3] = new Vector3f(min.x, max.y, min.z);
        obb[4] = new Vector3f(min.x, min.y, max.z);
        obb[5] = new Vector3f(max.x, min.y, max.z);
        obb[6] = new Vector3f(max.x, max.y, max.z);
        obb[7] = new Vector3f(min.x, max.y, max.z);
        return obb;
    }

    /*
    public Matrix4f getModel() {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.translate(position);
        matrix.rotate((float) Math.toRadians(rotation.x), AXIS_X);
        matrix.rotate((float) Math.toRadians(rotation.y), AXIS_Y);
        matrix.rotate((float) Math.toRadians(rotation.z), AXIS_Z);
        matrix.scale(new Vector3f(scale.x, scale.y, scale.z));
        return matrix;
    }*/

}
