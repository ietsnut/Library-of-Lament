package scene;

import content.House;
import content.Sky;
import game.Scene;
import property.Cylinder;
import property.Light;
import org.joml.Vector3f;
import property.Terrain;

public class Board extends Scene {

    public Board() {
        this.terrain = new Terrain("board");
        House house = new House("0");
        house.position.set(-16.776, 01.914, 19.506);
        entities.add(house);
        entities.add(new Sky("0", new Cylinder(8, 60), 5));
        entities.getLast().rotation.x = (float) (Math.random() * 90);
        entities.add(new Sky("0", new Cylinder(8, 80), 3));
        entities.getLast().rotation.y = (float) (Math.random() * 90);
        entities.add(new Sky("0", new Cylinder(8, 120), 1));
        entities.getLast().rotation.z = (float) (Math.random() * 90);
        /*
        for (int i = 0; i < 30; i+= 5) {
            Vase vase = new Vase("0");
            vase.scale.set(1/4f);
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

    }

}
