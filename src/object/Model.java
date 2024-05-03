package object;

import de.javagl.obj.*;
import org.joml.Vector3f;

import java.io.*;

public abstract class Model extends Entity {

    private File file;
    long modified;

    public Model(String model, boolean collidable) {
        super(model, collidable);
    }

    @Override
    public void load() {
        Obj obj;
        file = new File("resource/" + namespace + "/" + name + ".obj");
        modified = file.lastModified();
        try {
            obj = ObjReader.read(new FileInputStream(file));
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

    @Override
    public boolean reload() {
        if (file != null && file.exists() && file.lastModified() != modified) {
            modified = file.lastModified();
            return true;
        }
        return false;
    }

}
