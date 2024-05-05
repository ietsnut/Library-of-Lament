package object;

import de.javagl.obj.*;
import org.joml.Vector3f;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Model extends Entity {

    public Model(String model) {
        super(model);
    }

    public Model(String model, int states) {
        super(model, states);
    }

    @Override
    public void load() {
        Obj obj;
        try {
            obj = ObjReader.read(new FileInputStream("resource" + File.separator + type + File.separator + name + ".obj"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        obj         = ObjUtils.convertToRenderable(obj);
        indices     = ObjData.getFaceVertexIndicesArray(obj);
        float[] vertices    = ObjData.getVerticesArray(obj);
        this.vertices = new byte[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = (byte) Math.round(vertices[i]);
        }
        texCoords   = ObjData.getTexCoordsArray(obj, 2, true);
        normals     = ObjData.getNormalsArray(obj);
    }

}
