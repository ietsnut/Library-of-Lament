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

    public Model(String name) {
        this(name, name);
    }

    public Model(String name, String texture) {
        super(name);
        texture("resource/texture/" + texture + ".png");
    }

    @Override
    protected void load(Object... args) {
        Obj obj;
        try {
            obj = ObjReader.read(new FileInputStream("resource/model/" + args[0] + ".obj"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        obj = ObjUtils.convertToRenderable(obj);
        System.out.println(ObjUtils.createInfoString(obj));
        indices     = ObjData.getFaceVertexIndicesArray(obj);
        vertices    = ObjData.getVerticesArray(obj);
        texCoords   = ObjData.getTexCoordsArray(obj, 2, true);
        normals     = ObjData.getNormalsArray(obj);
    }

}
