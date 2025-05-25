package scene;

import content.Sky;
import content.Vase;
import engine.Scene;
import org.joml.Vector3f;
import property.Cylinder;
import property.Entity;
import property.Light;
import property.Terrain;

public class Forest extends Scene {

    public Forest() {
        terrain = new Terrain("dungeon_before");

        Vase vase = new Vase("2");
        vase.scale.set(1/24f);
        vase.position.set(1, 2, 10);
        vase.rotation.set(0, -45 ,0);
        entities.add(vase);


        Vase vase2 = new Vase("pillar2");
        vase2.scale.set(1/24f);
        vase2.position.set(-8, 4, 9);
        vase2.rotation.set(0, 0 ,0);
        entities.add(vase2);


        entities.add(new Sky("12", new Cylinder(8, 60), 5));
        entities.getLast().rotation.x = (float) (Math.random() * 90);
        entities.add(new Sky("12", new Cylinder(8, 80), 3));
        entities.getLast().rotation.y = (float) (Math.random() * 90);
        entities.add(new Sky("12", new Cylinder(8, 120), 1));
        entities.getLast().rotation.z = (float) (Math.random() * 90);



        /*
        for (int i = 0; i < 30; i+= 5) {
            Vase vase = new Vase("2");
            vase.scale.set(1/24f);
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
        }*/
        Light light1 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);
        Light light2 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light2);

        for (Entity entity : entities) {
            entity.update();
        }

        terrain.update();

    }

}
