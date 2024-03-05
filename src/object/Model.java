package object;

import de.javagl.obj.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.*;
import java.nio.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Model extends Entity {

    public Obj obj;

    public Texture texture;

    public Vector3f position = new Vector3f(0, 0, 0);
    public Vector3f rotation = new Vector3f(0, 0, 0);
    public Vector3f scale = new Vector3f(1, 1, 1);
    public float collider;

    public Model(String name) {
        super(name);
        texture = new Texture("resource/texture/" + name + ".png");
        System.out.println(Arrays.toString(texCoords));
        System.out.println(texture.image.getHeight());
    }


    @Override
    protected void load(String name) {
        try {
            obj = ObjUtils.convertToRenderable(ObjReader.read(new FileInputStream("resource/model/" + name + ".obj")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        indices = ObjData.getFaceVertexIndicesArray(obj);
        vertices = ObjData.getVerticesArray(obj);
        texCoords = ObjData.getTexCoordsArray(obj, 2, true);
        normals = ObjData.getNormalsArray(obj);
    }

    public Model position(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
        return this;
    }

    public Model rotation(float x, float y, float z) {
        this.rotation = new Vector3f(x, y, z);
        return this;
    }

    public Model scale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
        return this;
    }

    public Model scale(float s) {
        this.scale = new Vector3f(s, s, s);
        return this;
    }

    public Model collider(float radius) {
        this.collider = radius;
        return this;
    }

    public static final Vector3f AXIS_X = new Vector3f(1, 0, 0);
    public static final Vector3f AXIS_Y = new Vector3f(0, 1, 0);
    public static final Vector3f AXIS_Z = new Vector3f(0, 0, 1);

    public Matrix4f getTransformationMatrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.translate(position);
        matrix.rotate((float) Math.toRadians(rotation.x), AXIS_X);
        matrix.rotate((float) Math.toRadians(rotation.y), AXIS_Y);
        matrix.rotate((float) Math.toRadians(rotation.z), AXIS_Z);
        matrix.scale(new Vector3f(scale.x, scale.y, scale.z));
        return matrix;
    }

}
