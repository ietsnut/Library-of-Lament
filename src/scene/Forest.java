package scene;

import content.Door;
import content.Sky;
import content.Vase;
import engine.Scene;
import org.joml.Vector2f;
import org.joml.Vector3f;
import property.*;
import resource.Material;

public class Forest extends Scene {

    public Forest() {

        terrain = new Terrain("dungeon");

        Vase vase = new Vase("2");
        vase.scale.set(1/16f);
        vase.position.set(1, 2, 10);
        vase.rotation.set(0, -45 ,0);
        entities.add(vase);


        Entity vase2 = new Entity("pillar2");
        vase2.scale.set(1/16f);
        vase2.position.set(-8, 4, 9);
        vase2.rotation.set(0, 0 ,0);
        entities.add(vase2);

        Door arch = new Door("arch");
        arch.scale.set(1/16f);
        arch.position.set(-12, 4, 9);
        arch.rotation.set(0, 0 ,0);
        entities.add(arch);

        Entity wall = new Entity("wall");
        wall.scale.set(1/16f);
        wall.position.set(2, 2, 11);
        wall.rotation.set(0, 0 ,0);
        entities.add(wall);

        /*
        Vase vase3 = new Vase("train");
        vase3.scale.set(1/5f);
        vase3.position.set(1.5, 0, -2);
        entities.add(vase3);
        */

        /*
        entities.add(new Sky("12", new Cylinder(8, 60), 5));
        entities.getLast().rotation.x = (float) (Math.random() * 90);
        entities.add(new Sky("12", new Cylinder(8, 80), 3));
        entities.getLast().rotation.y = (float) (Math.random() * 90);
        entities.add(new Sky("12", new Cylinder(8, 120), 1));
        entities.getLast().rotation.z = (float) (Math.random() * 90);
         */

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
        Light light1 = new Light(new Vector3f(3, 3, 10), new Vector3f(1.0f, 0.2f, 0.02f), 1.25f);
        lights.add(light1);
        /*
        Light light2 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light2);
*/

    }

}
