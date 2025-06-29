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

import org.lwjgl.util.par.ParShapes;
import org.lwjgl.util.par.ParShapesMesh;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL40.*;

public class Mesh implements Resource {

    public int vao, ebo;
    public int[] vbo = new int[2];

    public int[] indices;
    public byte[] vertices;
    public float[] uvs;

    public int index;

    private IntBuffer indicesBuffer;
    private ByteBuffer verticesBuffer;
    private FloatBuffer uvsBuffer;

    public Collider collider;

    public static final Mesh PLANE = new Mesh() {
        @Override
        public void load() {
            this.vertices = new byte[]{
                    -1, -1, 0,  // bottom-left
                    1, -1, 0,  // bottom-right
                    1, 1, 0,  // top-right
                    -1, 1, 0   // top-left
            };
            this.indices = new int[]{
                    0, 1, 2,
                    2, 3, 0
            };
            this.uvs = new float[]{
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
            this.vertices = new byte[]{
                    // First plane vertices (X-Y plane)
                    -1, 0, 0,  // bottom-left
                    1, 0, 0,  // bottom-right
                    1, 2, 0,  // top-right
                    -1, 2, 0,  // top-left

                    // Second plane vertices (Z-Y plane)
                    0, 0, -1, // bottom-back
                    0, 0, 1, // bottom-front
                    0, 2, 1, // top-front
                    0, 2, -1  // top-back
            };

            this.indices = new int[]{
                    // First plane triangles
                    0, 1, 2,  // first triangle
                    2, 3, 0,  // second triangle

                    // Second plane triangles
                    4, 5, 6,  // first triangle
                    6, 7, 4   // second triangle
            };

            this.uvs = new float[]{
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

    private final String name;
    private final String type;

    public Mesh() {
        this(null, null);
    }

    public Mesh(String name) {
        this(name, null);
    }

    public Mesh(String name, String type) {
        this.type = type;
        this.name = name;
        if (dimensions() == 3) this.queue();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void load() {
        if (name == null) {
            return;
        }
        String file;
        if (type == null) {
            file = "/resources/" + name + ".obj";
        } else {
            file = "/resources/" + type + "/" + name + ".obj";
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
        obj = ObjUtils.convertToRenderable(obj);
        this.indices = ObjData.getFaceVertexIndicesArray(obj);
        float[] vertices = ObjData.getVerticesArray(obj);
        this.vertices = new byte[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = (byte) vertices[i];
        }
        this.uvs = ObjData.getTexCoordsArray(obj, 2, true);

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
        if (dimensions() == 3 && !(this instanceof Collider)) {
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
            this.vertices = new byte[]{
                    (byte) min.x, (byte) min.y, (byte) min.z, (byte) max.x, (byte) min.y, (byte) min.z, (byte) max.x, (byte) max.y, (byte) min.z, (byte) min.x, (byte) max.y, (byte) min.z, (byte) min.x, (byte) min.y, (byte) max.z, (byte) max.x, (byte) min.y, (byte) max.z, (byte) max.x, (byte) max.y, (byte) max.z, (byte) min.x, (byte) max.y, (byte) max.z
            };
            this.indices = new int[]{
                    0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 4, 1, 5, 2, 6, 3, 7
            };
            this.index = indices.length;
        }

        @Override
        public void unload() {
            indices = null;
            vertices = null;
            uvs = null;
            if (Mesh.this.type != null && Mesh.this.type.equalsIgnoreCase("terrain")) return;
            Mesh.this.uvs = null;
            Mesh.this.indices = null;
            Mesh.this.vertices = null;
        }

        @Override
        public String toString() {
            return Mesh.this.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Mesh mesh && this.name.equals(mesh.name);
    }

    public enum Shape {

    }

    public static Mesh knot(int scale, int slices, int stacks, float radius) {
        return new Mesh() {
            @Override
            public void load() {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    ParShapesMesh parMesh = ParShapes.par_shapes_create_trefoil_knot(slices, stacks, radius);
                    if (parMesh == null) {
                        Console.error("Failed to create cylinder mesh");
                        return;
                    }

                    // Extract vertices
                    FloatBuffer verticesFloatBuffer = parMesh.points(parMesh.npoints() * 3);
                    this.vertices = new byte[verticesFloatBuffer.remaining()];
                    for (int i = 0; i < this.vertices.length; i++) {
                        this.vertices[i] = (byte) (verticesFloatBuffer.get(i) * scale);
                    }

                    // Extract UV coordinates
                    FloatBuffer texCoordsBuffer = parMesh.tcoords(parMesh.npoints() * 2);
                    this.uvs = new float[texCoordsBuffer.remaining()];
                    texCoordsBuffer.get(this.uvs);

                    // Extract indices
                    IntBuffer indicesBuffer = parMesh.triangles(parMesh.ntriangles() * 3);
                    this.indices = new int[indicesBuffer.remaining()];
                    indicesBuffer.get(this.indices);

                    ParShapes.par_shapes_free_mesh(parMesh);
                }
            }

            @Override
            public String toString() {
                return "CYLINDER" + slices + "-" + stacks;
            }
        };
    }

}