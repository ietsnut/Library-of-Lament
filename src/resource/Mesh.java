package resource;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import object.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import property.Entity;

import java.io.*;
import java.nio.*;
import java.util.List;

import static org.lwjgl.opengl.GL40.*;

public class Mesh implements Resource {

    public static final Mesh PLANE = new Mesh() {
        @Override
        public void load() {
            this.vertices = new byte[] {
                    -1, 0, -1,  // bottom-left
                    1, 0, -1,  // bottom-right
                    1, 0,  1,  // top-right
                    -1, 0,  1   // top-left
            };
            this.indices = new int[] {
                    0, 1, 2,  // first triangle
                    2, 3, 0   // second triangle
            };
            this.texCoords = new float[] {
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
            this.index = indices.length;
        }
    };

    public int      vao = 0;
    public int[]    vbo = new int[4];

    public int[]    indices     = new int[0];
    public byte[]   vertices    = new byte[0];
    public float[]  normals     = new float[0];
    public float[]  texCoords   = new float[0];

    public int index;

    private static final IntBuffer   indicesBuffer      = BufferUtils.createIntBuffer(Short.MAX_VALUE);
    private static final ByteBuffer  verticesBuffer     = BufferUtils.createByteBuffer(Short.MAX_VALUE);
    private static final FloatBuffer normalsBuffer      = BufferUtils.createFloatBuffer(Short.MAX_VALUE);
    private static final FloatBuffer texCoordsBuffer    = BufferUtils.createFloatBuffer(Short.MAX_VALUE);

    public Collider collider;

    private final String file;

    public Mesh() {
        this.file = null;
        this.queue();
    }

    public Mesh(String type, String name) {
        this.file = "/resources/" + type + "/" + name + ".obj";
        this.queue();
    }

    public Mesh(String type, int state) {
        this(type, Integer.toString(state));
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
        obj                 = ObjUtils.convertToRenderable(obj);
        this.indices        = ObjData.getFaceVertexIndicesArray(obj);
        this.index          = indices.length;
        float[] vertices    = ObjData.getVerticesArray(obj);
        this.vertices       = new byte[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = (byte) Math.round(vertices[i]);
        }
        texCoords   = ObjData.getTexCoordsArray(obj, 2, true);
        normals     = ObjData.getNormalsArray(obj);
    }

    @Override
    public void buffer() {
        if (this.indices.length > 0) {
            indicesBuffer.clear();
            indicesBuffer.put(indices);
            indicesBuffer.flip();
        }
        if (this.vertices.length > 0) {
            verticesBuffer.clear();
            verticesBuffer.put(vertices);
            verticesBuffer.flip();
        }
        if (this.texCoords.length > 0) {
            texCoordsBuffer.clear();
            texCoordsBuffer.put(texCoords);
            texCoordsBuffer.flip();
        }
        if (this.normals.length > 0) {
            normalsBuffer.clear();
            normalsBuffer.put(normals);
            normalsBuffer.flip();
        }
    }

    @Override
    public boolean loaded() {
        return indices.length > 0 || vertices.length > 0 || texCoords.length > 0 || normals.length > 0;
    }

    @Override
    public boolean binded() {
        return vao != 0;
    }

    public static int buffer(int i, int l, Buffer buffer) {
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
        return vbo;
    }

    @Override
    public void bind() {
        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);
        if (indicesBuffer.hasRemaining()) {
            this.vbo[0] = buffer(0, 0, indicesBuffer);
        }
        if (verticesBuffer.hasRemaining()) {
            this.vbo[0] = buffer(0, 3, verticesBuffer);
        }
        if (texCoordsBuffer.hasRemaining()) {
            this.vbo[1] = buffer(1, 2, texCoordsBuffer);
        }
        if (normalsBuffer.hasRemaining()) {
            this.vbo[2] = buffer(2, 3, normalsBuffer);
        }
        glBindVertexArray(0);
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
        for (int vbo : vbo) {
            glDeleteBuffers(vbo);
        }
    }

    public class Collider extends Mesh {

        public Vector3f min;
        public Vector3f max;
        public float size;

        private static int ID = 0;

        public Collider() {
            super("collider", ID++);
        }

        @Override
        public void load() {
            byte[] vertices = Mesh.this.vertices;
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
            this.index = indices.length;
        }

        @Override
        public void unload() {
            if (file.contains("terrain")) return;
            Mesh.this.indices     = null;
            Mesh.this.vertices    = null;
            Mesh.this.texCoords   = null;
            Mesh.this.normals     = null;
            indices     = null;
            vertices    = null;
            texCoords   = null;
            normals     = null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Mesh mesh && this.file.equals(mesh.file);
    }

}