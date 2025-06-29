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
    public int[]    vbo = new int[2];

    public int[]    indices;
    public byte[]   vertices;
    public float[]  uvs;

    public int index;

    private IntBuffer   indicesBuffer;
    private ByteBuffer  verticesBuffer;
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
        @Override
        public String toString() {
            return "QUAD";
        }
    };

    public static final Mesh PLANE = new Mesh() {
        @Override
        public void load() {
            this.vertices = new byte[] {
                    -1, -1, 0,  // bottom-left
                    1, -1, 0,  // bottom-right
                    1,  1, 0,  // top-right
                    -1,  1, 0   // top-left
            };
            this.indices = new int[] {
                    0, 1, 2,
                    2, 3, 0
            };
            this.uvs = new float[] {
                    0, 1,  // bottom-left
                    1, 1,  // bottom-right
                    1, 0,  // top-right
                    0, 0   // top-left
            };
        }

        @Override
        public String toString() {
            return "PLANE";
        }
    };

    public static final Mesh X_PLANE = new Mesh() {
        @Override
        public void load() {
            this.vertices = new byte[] {
                    // First plane vertices (X-Y plane)
                    -1, 0, 0,  // bottom-left
                    1, 0, 0,  // bottom-right
                    1, 2, 0,  // top-right
                    -1, 2, 0,  // top-left

                    // Second plane vertices (Z-Y plane)
                    0, 0, -1, // bottom-back
                    0, 0,  1, // bottom-front
                    0, 2,  1, // top-front
                    0, 2, -1  // top-back
            };

            this.indices = new int[] {
                    // First plane triangles
                    0, 1, 2,  // first triangle
                    2, 3, 0,  // second triangle

                    // Second plane triangles
                    4, 5, 6,  // first triangle
                    6, 7, 4   // second triangle
            };

            this.uvs = new float[] {
                    // First plane UVs
                    0, 1,  // bottom-left
                    1, 1,  // bottom-right
                    1, 0,  // top-right
                    0, 0,  // top-left

                    // Second plane UVs
                    0, 1,  // bottom-back
                    1, 1,  // bottom-front
                    1, 0,  // top-front
                    0, 0   // top-back
            };
        }
        @Override
        public String toString() {
            return "X_PLANE";
        }
    };

    public Mesh() {
        this.file = null;
        if (dimensions() == 3) this.queue();
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
        return file.replace("/resources/", "");
    }

    @Override
    public void load() {
        // TODO: check if a binary encoded file is present, if yes, decode and load it, if not, load the model and encode it for next time
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
            Console.error(e, "Failed to load", file);
            return;
        }
        obj             = ObjUtils.convertToRenderable(obj);
        this.indices    = ObjData.getFaceVertexIndicesArray(obj);
        float[] vertices= ObjData.getVerticesArray(obj);
        this.vertices = new byte[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = (byte) vertices[i];
        }
        this.uvs        = ObjData.getTexCoordsArray(obj, 2, true);

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
    }

    @Override
    public boolean linked() {
        return vao != 0;
    }

    public int dimensions() {
        return 3;
    }

    @Override
    public void link() {
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
        if (file != null && !(this instanceof Collider)) {
            this.collider = new Collider();
        }
    }

    @Override
    public void unload() {
        return;
    }

    @Override
    public void unlink() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(ebo);
        for (int id : vbo) {
            glDeleteBuffers(id);
        }
    }

    @Override
    public void bind() {
        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
    }

    @Override
    public void unbind() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public void encode() {
        //TODO: save the mesh data as bytes to a file next to the model, which will be used next time to load it if available
    }

    public void decode() {
        //TODO: decode the available mesh data straight from a binary file
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
            if (file.contains("terrain")) return;
            Mesh.this.uvs         = null;
            Mesh.this.indices     = null;
            Mesh.this.vertices    = null;
        }

        @Override
        public String toString() {
            return "collider " + Mesh.this.file;
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Mesh mesh && this.file.equals(mesh.file);
    }

}