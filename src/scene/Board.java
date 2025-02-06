package scene;

import content.Character;
import content.Sky;
import game.Scene;
import property.Cylinder;
import property.Light;
import org.joml.Vector3f;
import property.Terrain;
import resource.Mesh;

public class Board extends Scene {

    public Board() {
        terrain = new Terrain("forest");

        Character oracle = new Character("oracle");
        oracle.position.set(5f, 0f, 5f);
        oracle.rotation.y = 90;
        entities.add(oracle);

        entities.add(new Sky("0", new Cylinder(16, 130)));
        entities.add(new Sky("0", new Cylinder(16, 150)));
        entities.add(new Sky("0", new Cylinder(16, 160)));

        entities.add(new Sky("0", new Cylinder(16, 130)));
        entities.add(new Sky("0", new Cylinder(16, 150)));
        entities.add(new Sky("0", new Cylinder(16, 160)));

    /*
        Light light1 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);
        Light light2 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light2);*/

    }

}
