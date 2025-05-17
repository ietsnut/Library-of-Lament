package resource;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import engine.Console;
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

    private final String file;

    public Mesh() {
        this.file = null;
        this.queue();
    }

    public Mesh(String name) {
        this.file = "/resources/automata/" + name + ".obj";
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
            Console.error("Failed to load", file);
            return;
        }
        obj             = ObjUtils.convertToRenderable(obj);
        this.indices    = ObjData.getFaceVertexIndicesArray(obj);
        float[] vertices = ObjData.getVerticesArray(obj);
        this.vertices = new byte[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = (byte) vertices[i];
        }
        this.uvs        = ObjData.getTexCoordsArray(obj, 2, true);
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
            verticesBuffer = BufferUtils.createByteBuffer(vertices.length);
            verticesBuffer.put(vertices).flip();
        }
        if (uvs.length > 0) {
            uvsBuffer = BufferUtils.createFloatBuffer(uvs.length);
            uvsBuffer.put(uvs).flip();
        }
        if (normals.length > 0) {
            normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
            normalsBuffer.put(normals).flip();
        }
    }

    @Override
    public boolean loaded() {
<<<<<<< HEAD
<<<<<<< HEAD
        return indices.length > 0 && vertices.length > 0 && uvs.length > 0 && normals.length > 0;
=======
        return indices.length > 0 || vertices.length > 0 || texCoords.length > 0 || normals.length > 0;
>>>>>>> parent of 9f378cf (basic scene switching)
=======
        return indices.length > 0 || vertices.length > 0 || texCoords.length > 0 || normals.length > 0;
>>>>>>> parent of 9f378cf (basic scene switching)
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
            glVertexAttribPointer(0, 3, GL_BYTE, false, 0, 0);
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
    }

    @Override
    public void unload() {
        this.indices    = null;
        this.vertices   = null;
        this.uvs        = null;
        this.normals    = null;
    }

    @Override
    public void unbind() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(ebo);
        for (int id : vbo) {
            glDeleteBuffers(id);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Mesh mesh && this.file.equals(mesh.file);
    }

}