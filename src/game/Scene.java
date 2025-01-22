package game;

import property.Light;
import property.Entity;
import property.Machine;
import property.Terrain;

import java.util.ArrayList;

public abstract class Scene implements Machine {

    public final ArrayList<Entity> entities = new ArrayList<>();
    public final ArrayList<Light>  lights   = new ArrayList<>();
    public Terrain terrain;

    public Scene() {
        start();
    }

    @Override
    public void turn() {
        for (Entity entity : entities) {
            entity.updateModel();
        }
    }

}
