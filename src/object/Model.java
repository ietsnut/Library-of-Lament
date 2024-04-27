package object;

import de.javagl.obj.*;
import java.io.*;

public class Model extends Entity {

    Obj obj;

    public Model(String model) {
        super(model, true);
        enqueue();
    }

    public Model(String model, Obj obj) {
        super(model, true);
        this.obj = obj;
        enqueue();
    }

    @Override
    public void load() {
        if (obj == null) {
            try {
                obj = ObjReader.read(new FileInputStream("resource/model/" + name + ".obj"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        obj         = ObjUtils.convertToRenderable(obj);
        //System.out.println(ObjUtils.createInfoString(obj));
        indices     = ObjData.getFaceVertexIndicesArray(obj);
        vertices    = ObjData.getVerticesArray(obj);
        texCoords   = ObjData.getTexCoordsArray(obj, 2, true);
        normals     = ObjData.getNormalsArray(obj);
    }

}
