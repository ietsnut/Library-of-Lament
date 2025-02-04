package game;

import object.Camera;
import org.joml.Matrix4f;
import property.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Scene {

    public final ArrayList<Entity> entities = new ArrayList<>();
    public final ArrayList<Light>  lights   = new ArrayList<>();
    public Terrain terrain;

    public void clear() {
        entities.clear();
        lights.clear();
        terrain = null;
        System.gc();
    }

}
