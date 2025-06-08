package engine;

import property.*;

import java.util.ArrayList;

public abstract class Scene {

    public final ArrayList<Entity> foreground = new ArrayList<>();
    public final ArrayList<Entity> background = new ArrayList<>();

    public Terrain terrain;

    public void clear() {
        for (Entity entity : foreground) {
            entity.unbind();
        }
        for (Entity entity : background) {
            entity.unbind();
        }
        foreground.clear();
        background.clear();
        terrain.unbind();
        System.gc();
    }

}
