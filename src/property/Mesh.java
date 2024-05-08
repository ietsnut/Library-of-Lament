package property;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import object.Camera;
import object.Entity;
import object.Load;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.*;

import static org.lwjgl.opengl.GL40.*;

public class Mesh extends Resource {

    public int      vao = 0;
    public int[]    vbo = new int[4];

    public int[]    indices     = new int[0];
    public byte[]   vertices    = new byte[0];
    public float[]  normals     = new float[0];
    public float[]  texCoords   = new float[0];

    private IntBuffer   indicesBuffer   = null;
    private ByteBuffer  verticesBuffer  = null;
    private FloatBuffer normalsBuffer   = null;
    private FloatBuffer texCoordsBuffer = null;

    public Collider collider;

    public Mesh() {
        super();
    }

    public Mesh(Entity entity) {
        super(entity.id, entity.type);
    }

    public Mesh(byte id, String type) {
        super(id, type);
    }

    public Mesh(byte id, String type, String state) {
        super(id, type, state);
    }

    @Override
    public void load() {
        Obj obj;
        try {
            if (state != null) {
                obj = ObjReader.read(new FileInputStream("resource" + File.separator + type + File.separator + id + "_" + state + ".obj"));
            } else {
                obj = ObjReader.read(new FileInputStream("resource" + File.separator + type + File.separator + id + ".obj"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        obj         = ObjUtils.convertToRenderable(obj);
        indices     = ObjData.getFaceVertexIndicesArray(obj);
        float[] vertices    = ObjData.getVerticesArray(obj);
        this.vertices       = new byte[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = (byte) Math.round(vertices[i]);
        }
        texCoords   = ObjData.getTexCoordsArray(obj, 2, true);
        normals     = ObjData.getNormalsArray(obj);
    }

    @Override
    public void postload() {
        if (this.indices.length > 0) {
            indicesBuffer = BufferUtils.createIntBuffer(indices.length);
            indicesBuffer.put(indices);
            indicesBuffer.flip();
        }
        if (this.vertices.length > 0) {
            verticesBuffer = BufferUtils.createByteBuffer(vertices.length);
            verticesBuffer.put(vertices);
            verticesBuffer.flip();
        }
        if (this.texCoords.length > 0) {
            texCoordsBuffer = BufferUtils.createFloatBuffer(texCoords.length);
            texCoordsBuffer.position(0);
            texCoordsBuffer.put(texCoords);
            texCoordsBuffer.flip();
        }
        if (this.normals.length > 0) {
            normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
            normalsBuffer.position(0);
            normalsBuffer.put(normals);
            normalsBuffer.flip();
        }
        if (!(this instanceof Collider) && type != null) {
            this.collider = new Collider();
        }
    }

    private void buffer(int i, int l, Buffer buffer) {
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
        this.vbo[i] = vbo;
    }

    @Override
    public void bind() {
        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);
        if (this.indicesBuffer != null) {
            buffer(0, 0, this.indicesBuffer);
        }
        if (this.verticesBuffer != null) {
            buffer(0, type == null ? 2 : 3, this.verticesBuffer);
        }
        if (this.texCoordsBuffer != null) {
            buffer(1, 2, this.texCoordsBuffer);
        }
        if (this.normalsBuffer != null) {
            buffer(2, 3, this.normalsBuffer);
        }
        glBindVertexArray(0);
    }

    @Override
    public void postbind() {

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

        public Collider() {
            super();
        }

        @Override
        public void load() {
            byte[] vertices = Mesh.this.vertices;
            min = new Vector3f(Byte.MAX, Byte.MAX, Byte.MAX);
            max = new Vector3f(Byte.MIN, Byte.MIN, Byte.MIN);
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
        }

        private Float collision(Entity entity) {
            if (collider == null) {
                return -1f;
            }
            Matrix4f inverseModelMatrix = new Matrix4f();
            entity.model.invert(inverseModelMatrix);
            Vector4f rayOriginModelSpace = new Vector4f(Camera.position.x, Camera.position.y, Camera.position.z, 1.0f);
            inverseModelMatrix.transform(rayOriginModelSpace);
            Vector3f ray = new Vector3f(Camera.forward()).negate().normalize();
            Vector4f rayDirectionModelSpace = new Vector4f(ray.x, ray.y, ray.z, 0.0f);
            inverseModelMatrix.transform(rayDirectionModelSpace);
            Vector3f directionNormalized = new Vector3f(rayDirectionModelSpace.x, rayDirectionModelSpace.y, rayDirectionModelSpace.z).normalize();
            Vector3f invDir = new Vector3f(1.0f / directionNormalized.x, 1.0f / directionNormalized.y, 1.0f / directionNormalized.z);
            float t1 = (collider.min.x - rayOriginModelSpace.x) * invDir.x;
            float t2 = (collider.max.x - rayOriginModelSpace.x) * invDir.x;
            float t3 = (collider.min.y - rayOriginModelSpace.y) * invDir.y;
            float t4 = (collider.max.y - rayOriginModelSpace.y) * invDir.y;
            float t5 = (collider.min.z - rayOriginModelSpace.z) * invDir.z;
            float t6 = (collider.max.z - rayOriginModelSpace.z) * invDir.z;
            float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
            float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
            if (tmax < 0) {
                return -1f;
            }
            if (tmin > tmax) {
                return -1f;
            }
            return Math.max(tmin, 0);
        }

    /*
    public boolean lookingAt(float distance) {
        return collision() > 0 && distance() < distance;
    }

    public float distance() {
        return new Vector3f(Camera.position).sub(this.position).length();
    }

    public static Entity lookingAt(List<Entity> entities, float distance) {
        float min = Float.MAX_VALUE;
        Entity closest = null;
        for (Entity entity : entities) {
            if (entity.collidable() && entity.lookingAt(distance) && entity.distance() < min) {
                min = entity.distance();
                closest = entity;
            }
        }
        return closest;
    }

    public boolean collidable() {
        return this.collidable && this.collider != null && this.loaded && this.collider.bound();
    }
     */

    }

}