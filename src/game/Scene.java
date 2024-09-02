package game;

import property.Entity;
import property.Terrain;

import java.util.ArrayList;

public abstract class Scene {

    public final ArrayList<Entity> entities = new ArrayList<>();
    public Terrain terrain;

}
