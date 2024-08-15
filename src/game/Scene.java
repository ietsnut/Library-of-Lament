package game;

import property.Terrain;
import object.*;
import property.Entity;
import property.Machine;
import property.Resource;

import java.util.ArrayList;
import java.util.List;

public abstract class Scene implements Machine {

    public List<Light>  lights      = new ArrayList<>();
    public List<Entity> entities    = new ArrayList<>();
    public Terrain      terrain;

    public <T extends Entity> List<T> getEntities(Class<T> type) {
        final String t = type.getSimpleName().toLowerCase();
        final List<T> result = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.type.equalsIgnoreCase(t)) {
                result.add(type.cast(entity));
            }
        }
        return result;
    }

}
