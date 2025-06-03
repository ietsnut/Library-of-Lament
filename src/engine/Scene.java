package engine;

import property.*;

import java.util.ArrayList;

public abstract class Scene {

    public final ArrayList<Entity>  entities = new ArrayList<>();
    public final ArrayList<Light>   lights   = new ArrayList<>();
    public final ArrayList<GUI>     guis   = new ArrayList<>();
    public Terrain terrain;

    public void clear() {
        for (Entity entity : entities) {
            entity.unbind();
        }
        entities.clear();
        lights.clear();
        for (GUI gui : guis) {
            gui.unbind();
        }
        guis.clear();
        terrain.unbind();
        System.gc();
    }

}
