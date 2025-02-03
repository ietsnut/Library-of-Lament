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
            this.vertices = new float[] {
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

    /* TODO: add more static meshes/constants:
     *  - Plane 1x1
     *  - X-shape (for foliage)
     *  - Box/Rectangle 1x1x1 (same 6 sides)
     *  - Box/Rectangle (unfolded, 6 different sides)
     *  Meshes with constructor:
     *  - Box/Rectangle (width, height and depth for dimensions)
     *  - Cylinder (radius and amount of sides)
     *  - Plane (width and height)
     * */

    /*
    * TODO: add entity types:
    *  - Billboard (plane always facing player)
    *  - Sky (rotating around player)
    *  - 
    * */

    public int      vao, ebo;
    public int[]    vbo = new int[3];

    public int[]    indices     = new int[0];
    public float[]  vertices    = new float[0];
    public float[]  normals     = new float[0];
    public float[]  texCoords   = new float[0];

    public int index;

    private IntBuffer   indicesBuffer;
    private FloatBuffer verticesBuffer;
    private FloatBuffer normalsBuffer;
    private FloatBuffer texCoordsBuffer;

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
        this.vertices   = ObjData.getVerticesArray(obj);
        this.texCoords  = ObjData.getTexCoordsArray(obj, 2, true);
        this.normals    = ObjData.getNormalsArray(obj);
        this.index      = (indices != null) ? indices.length : 0;
    }

    @Override
    public void buffer() {
        if (indices.length > 0) {
            indicesBuffer = BufferUtils.createIntBuffer(indices.length);
            indicesBuffer.put(indices).flip();
        }
        if (vertices.length > 0) {
            verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
            verticesBuffer.put(vertices).flip();
        }
        if (texCoords.length > 0) {
            texCoordsBuffer = BufferUtils.createFloatBuffer(texCoords.length);
            texCoordsBuffer.put(texCoords).flip();
        }
        if (normals.length > 0) {
            normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
            normalsBuffer.put(normals).flip();
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
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        }
        if (texCoordsBuffer != null && texCoordsBuffer.hasRemaining()) {
            vbo[1] = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
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
        if (texCoordsBuffer != null) {
            texCoordsBuffer.clear();
            texCoordsBuffer = null;
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

        private static int ID = 0;

        public Collider() {
            super("collider", ID++);
        }

        @Override
        public void load() {
            float[] vertices = Mesh.this.vertices;
            min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
            for (int i = 0; i < vertices.length; i += 3) {
                min.set(Math.min(min.x, vertices[i]), Math.min(min.y, vertices[i + 1]), Math.min(min.z, vertices[i + 2]));
                max.set(Math.max(max.x, vertices[i]), Math.max(max.y, vertices[i + 1]), Math.max(max.z, vertices[i + 2]));
            }
            size = new Vector3f(max).sub(min).length();
            this.vertices = new float[] {
                     min.x, min.y, min.z, max.x, min.y, min.z, max.x, max.y, min.z, min.x, max.y, min.z, min.x, min.y, max.z, max.x, min.y, max.z, max.x, max.y, max.z, min.x, max.y,max.z
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