package engine;

import property.*;

import java.util.ArrayList;

public abstract class Scene {

    public final ArrayList<Entity>  entities = new ArrayList<>();

    public Terrain terrain;

    public void clear() {
        for (Entity entity : entities) {
            entity.unbind();
        }
        entities.clear();
        terrain.unbind();
        System.gc();
    }

}
