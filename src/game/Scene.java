package game;

import content.Character;
import content.Terrain;
import content.Vase;
import object.*;
import org.joml.Vector3f;
import property.Worker;

import java.util.ArrayList;
import java.util.List;

import static object.Entity.*;

public class Scene implements Worker {

    public List<Light>  lights      = new ArrayList<>();

    public List<Entity> entities    = new ArrayList<>();

    public Entity last;

    public Scene() {

        entities.add(new Terrain((byte) 0));

        for (int i = 0; i < 30; i+=3) {
            Vase vase = new Vase((byte) 0);
            vase.scale = MICRO;
            System.out.println(vase.scale);
            float x = (float) (Math.random() * i);
            float z = 1.5f;
            if (Math.random() > 0.5) {
                x = -x;
            }
            if (Math.random() > 0.5) {
                z = -z;
            }
            vase.position.set((byte) x, (byte) 0, (byte) z);
            //entities.add(vase);
        }

        Character character = new Character((byte) 0);
        character.scale = DECI;
        character.position.set((byte) 0, (byte) 0, (byte) 1);
        entities.add(character);

        Light light1 = new Light(new Vector3f(-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);

        Light light2 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light2);

        start();

    }

    @Override
    public void work() {
        lights.getFirst().position = new Vector3f(Camera.position);
        for (Entity entity : entities) {
            entity.update();
            entity.remodel();
        }
        for (Entity entity : entities) {
            if (entity instanceof Vase vase) {
                //vase.rotation.y += 1;
            }
        }
        /*
        Entity active = Entity.lookingAt(entities, 30f);
        boolean clicked = Control.isClicked();
        if (active != last) {
            if (last instanceof Interactive interactive) {
                interactive.onExit();
            }
            last = active;
            if (active instanceof Interactive interactive) {
                interactive.onEnter();  // Call onEnter when a new entity becomes the active target
            }
        }
        if (active instanceof Interactive interactive) {
            if (clicked) {
                interactive.onClick();
            }
            if (Control.isHolding()) {
                interactive.onHold();
            }
        }
         */
    }

    public <T extends Entity> T getEntity(Class<T> type) {
        for (Entity entity : entities) {
            if (type.isInstance(entity)) {
                return type.cast(entity);
            }
        }
        return null;
    }

    public List<Entity> getEntities(Class<? extends Entity> type) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities) {
            if (type.isInstance(entity)) {
                result.add(entity);
            }
        }
        return result;
    }

}
