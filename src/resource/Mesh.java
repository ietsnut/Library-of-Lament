package resource;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import engine.Console;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.*;

import static org.lwjgl.opengl.GL40.*;

public class Mesh implements Resource {

    public int      vao, ebo;
    public int[]    vbo = new int[3];

    public int[]    indices;
    public byte[]   vertices;
    public float[]  normals;
    public float[]  uvs;

    public int index;

    private IntBuffer   indicesBuffer;
    private ByteBuffer  verticesBuffer;
    private FloatBuffer normalsBuffer;
    private FloatBuffer uvsBuffer;

    public Collider collider;

    private final String file;

    public static final Mesh QUAD = new Mesh() {
        @Override
        public void load() {
            vertices = new byte[] {-1, 1, -1, -1, 1, 1, 1, -1};
            indices  = new int[] {0, 1, 2, 2, 1, 3};
            uvs = new float[]{ 0, 0, 1, 0, 1, 1, 0, 1 };
        }
        @Override
        public int dimensions() {
            return 2;
        }
    };

    public static final Mesh PLANE = new Mesh() {
        @Override
        public void load() {
            this.vertices = new byte[] {
                    -10, 0, -10,  // bottom-left
                    10, 0, -10,  // bottom-right
                    10, 0,  10,  // top-right
                    -10, 0,  10   // top-left
            };
            this.indices = new int[] {
                    0, 1, 2,  // first triangle
                    2, 3, 0   // second triangle
            };
            this.uvs = new float[] {
                    0, 0,  // bottom-left
                    1, 0,  // bottom-right
                    1, 1,  // top-right
                    0, 1   // top-left
            };
            this.normals = new float[] {
                    0, 1, 0,  // bottom-left
                    0, 1, 0,  // bottom-right
                    0, 1, 0,  // top-right
                    0, 1, 0   // top-left
            };
        }
    };

    public Mesh() {
        this.file = null;
        this.queue();
    }

    public Mesh(String name) {
        this.file = "/resources/" + name + ".obj";
        this.queue();
    }

    public Mesh(String type, String name) {
        this.file = "/resources/" + type + "/" + name + ".obj";
        this.queue();
    }

    public Mesh(String type, String name, int state) {
        this.file = "/resources/" + type + "/" + name + "_" + state + ".obj";
        this.queue();
    }

    @Override
    public String toString() {
        return file;
    }

    @Override
    public void load() {
        if (file == null) {
            return;
        }
        Obj obj;
        try (InputStream in = getClass().getResourceAsStream(file)) {
            assert in != null;
            try (BufferedInputStream bis = new BufferedInputStream(in)) {
                obj = ObjReader.read(bis);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mesh: " + file, e);
        }
        obj             = ObjUtils.convertToRenderable(obj);
        this.indices    = ObjData.getFaceVertexIndicesArray(obj);
        float[] vertices= ObjData.getVerticesArray(obj);
        this.vertices = new byte[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = (byte) vertices[i];
        }
        this.uvs        = ObjData.getTexCoordsArray(obj, 2, true);
        this.normals    = ObjData.getNormalsArray(obj);
    }

    @Override
    public void buffer() {
        this.index = (indices != null) ? indices.length : 0;
        if (indices != null && indices.length > 0) {
            indicesBuffer = BufferUtils.createIntBuffer(indices.length).put(indices).flip();
        }
        if (vertices != null && vertices.length > 0) {
            verticesBuffer = BufferUtils.createByteBuffer(vertices.length).put(vertices).flip();
        }
        if (uvs != null && uvs.length > 0) {
            uvsBuffer = BufferUtils.createFloatBuffer(uvs.length).put(uvs).flip();
        }
        if (normals != null && normals.length > 0) {
            normalsBuffer = BufferUtils.createFloatBuffer(normals.length).put(normals).flip();
        }
    }

    @Override
    public boolean loaded() {
        return index != 0;
    }

    @Override
    public boolean binded() {
        return vao != 0;
    }

    public int dimensions() {
        return 3;
    }

    @Override
    public void bind() {
        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);
        if (indicesBuffer != null && indicesBuffer.hasRemaining()) {
            ebo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        }
        if (verticesBuffer != null && verticesBuffer.hasRemaining()) {
            vbo[0] = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, dimensions(), GL_BYTE, false, 0, 0);
        }
        if (uvsBuffer != null && uvsBuffer.hasRemaining()) {
            vbo[1] = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            glBufferData(GL_ARRAY_BUFFER, uvsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        }
        if (normalsBuffer != null && normalsBuffer.hasRemaining()) {
            vbo[2] = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        }
        glBindVertexArray(0);
        if (indicesBuffer != null) {
            indicesBuffer.clear();
            indicesBuffer = null;
        }
        if (verticesBuffer != null) {
            verticesBuffer.clear();
            verticesBuffer = null;
        }
        if (uvsBuffer != null) {
            uvsBuffer.clear();
            uvsBuffer = null;
        }
        if (normalsBuffer != null) {
            normalsBuffer.clear();
            normalsBuffer = null;
        }
        if (file != null && !(this instanceof Collider)) {
            this.collider = new Collider();
        }
    }

    @Override
    public void unload() {
        return;
    }

    @Override
    public void unbind() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(ebo);
        for (int id : vbo) {
            glDeleteBuffers(id);
        }
    }

    public class Collider extends Mesh {

        public Vector3f min;
        public Vector3f max;
        public float size;

        public Collider() {
            super();
        }

        @Override
        public void load() {
            byte[] vertices = Mesh.this.vertices;
            min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
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
            this.index = indices.length;
        }

        @Override
        public void unload() {
            indices     = null;
            vertices    = null;
            uvs         = null;
            normals     = null;
            if (file.contains("terrain")) return;
            Mesh.this.indices     = null;
            Mesh.this.vertices    = null;
            Mesh.this.uvs         = null;
            Mesh.this.normals     = null;
        }

        @Override
        public String toString() {
            return "collider of " + Mesh.this.file;
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Mesh mesh && this.file.equals(mesh.file);
    }

}