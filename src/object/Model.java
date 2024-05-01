package object;

import de.javagl.obj.*;
import org.joml.Vector3f;

import java.io.*;

public class Model extends Entity {

    public Model(String model) {
        super(model, true);
        enqueue();
    }

    @Override
    public void load() {
       obj("model");
    }

    protected void obj(String model) {
        Obj obj;
        try {
            obj = ObjReader.read(new FileInputStream("resource/" + model + "/" + name + ".obj"));
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
        System.out.println(ObjUtils.createInfoString(obj));
    }

}
