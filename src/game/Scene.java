package game;

import content.Terrain;
import content.Vase;
import object.*;
import org.joml.Vector3f;
import property.Entity;
import property.Machine;

import java.util.ArrayList;
import java.util.List;

import static property.Entity.*;

public class Scene implements Machine {

    public List<Light>  lights      = new ArrayList<>();

    public List<Entity> entities    = new ArrayList<>();

    public Entity active;
    public Entity last;

    public Scene() {

        entities.add(new Terrain((byte) 0));

        for (int i = 0; i < 30; i+=3) {
            Vase vase = new Vase((byte) 0);
            vase.scale = MICRO;
            float x = (float) (Math.random() * i);
            float z = 1.5f;
            if (Math.random() > 0.5) {
                x = -x;
            }
            if (Math.random() > 0.5) {
                z = -z;
            }
            vase.position.set(x,  0, z);
            entities.add(vase);
        }

        /*
        Character character = new Character((byte) 0);
        character.scale = DECI;
        character.position.set( 0, 0, 1);
        entities.add(character);
        */

        Light light1 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);

        Light light2 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light2);

        start(1);

    }

    @Override
    public void process() {
        System.out.println("Scene.process");
        /*
        float min = Float.MAX_VALUE;
        Entity closest = null;
        for (Entity entity : entities) {
            Mesh mesh = entity.meshes.get(entity.mesh);
            if (!mesh.collider.bound) {
                continue;
            }
            System.out.println("checking " + mesh);
            System.out.println(mesh.collider);
            System.out.println("fdf");
            if (entity.interacive && mesh.collider.lookingAt(30f, entity) && mesh.collider.distance(entity) < min) {
                min = mesh.collider.distance(entity);
                closest = entity;
            }
        }
        active = closest;

         */
        /*
        active = Mesh.Collider.lookingAt(entities, 30f);
        System.out.println(active);
        boolean clicked = Control.isClicked();
        if (active != last) {
            if (last instanceof Interactive interactive) {
                interactive.exit();
            }
            last = active;
            if (active instanceof Interactive interactive) {
                interactive.enter();  // Call onEnter when a new entity becomes the active target
            }
        }
        if (active instanceof Interactive interactive) {
            if (clicked) {
                interactive.click();
            }
            if (Control.isHolding()) {
                interactive.hold();
            }
        }*/
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
