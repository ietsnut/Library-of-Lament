package game;

import object.Camera;
import property.*;

import javax.crypto.Mac;
import java.util.ArrayList;

public abstract class Scene implements Machine {

    public final ArrayList<Entity> entities = new ArrayList<>();
    public final ArrayList<Light>  lights   = new ArrayList<>();

    public Entity intersecting;
    public Entity inside;

    public Terrain terrain;

    @Override
    public void turn() {
        float distance = Float.MAX_VALUE;
        Entity intersecting = null;
        boolean inside = false;
        for (Entity entity : entities) {
            entity.updateModel();
            if (entity instanceof Interactive interactive) {
                if (Camera.inside(entity)) {
                    if (this.inside == null) {
                        this.inside = entity;
                        interactive.enter();
                    }
                    inside = true;
                }
                if (Camera.collision(entity) > 0 && Camera.distance(entity) < 5f && Camera.distance(entity) < distance) {
                    distance = Camera.distance(entity);
                    intersecting = entity;
                }
            }
        }
        this.intersecting = intersecting;
        if (intersecting instanceof Interactive interactive) {
            if (Control.isClicked()) {
                interactive.click();
            }
        }
        if (!inside) {
            if (this.inside != null && this.inside instanceof Interactive interactive) {
                interactive.leave();
            }
            this.inside = null;
        }
        //intersecting = Camera.closest(entities);
    }

    public void clear() {
        entities.clear();
        lights.clear();
        intersecting = null;
        terrain = null;
    }

}
