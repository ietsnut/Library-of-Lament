package object;

import de.javagl.obj.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
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

    public Model(String name, int frames) {
        this(name, name, frames);
    }

    public Model(String name, String texture) {
        this(name, texture, 1);
    }

    public Model(String name, String texture, int frames) {
        super(name);
        if (frames == 1) {
            texture("texture", texture);
        } else {
            texture("texture", texture, frames);
        }
        texture("texture", "noise");
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
