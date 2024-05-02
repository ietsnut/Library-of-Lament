package object;

import de.javagl.obj.*;
import org.joml.Vector3f;

import java.io.*;

public abstract class Model extends Entity {

    private File file;
    long last_modified;

    public Model(String namespace, String model, boolean collidable) {
        super(namespace, model, collidable);
        file = new File("resource/" + namespace + "/" + name + ".obj");
        last_modified = file.lastModified();
    }

    @Override
    public void load() {
        Obj obj;
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
        if (file != null && file.exists() && file.lastModified() != last_modified) {
            last_modified = file.lastModified();
            return true;
        }
        return false;
    }

}
