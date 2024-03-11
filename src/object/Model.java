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

    public float collider;

    public Model(String name) {
        super(name);
        this.texture = new Texture("resource/texture/" + name + ".png");
    }

    public Model(String name, String texture) {
        super(name);
        this.texture = new Texture(texture);
    }

    @Override
    protected void load(String name) {
        Obj obj;
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

    public Model collider(float radius) {
        this.collider = radius;
        return this;
    }

}
