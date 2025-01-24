package game;

import object.Camera;
import property.*;

import javax.crypto.Mac;
import java.util.ArrayList;

public abstract class Scene {

    public final ArrayList<Entity> entities = new ArrayList<>();
    public final ArrayList<Light>  lights   = new ArrayList<>();
    public Terrain terrain;

    public void clear() {
        entities.clear();
        lights.clear();
        terrain = null;
    }

}
