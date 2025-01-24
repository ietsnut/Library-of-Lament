package scene;

import content.Creature;
import content.Sky;
import content.Vase;
import game.Scene;
import property.Light;
import org.joml.Vector3f;
import property.Machine;
import property.Terrain;
import resource.Mesh;

public class Sewer extends Scene {

    public Sewer() {
        this.terrain = new Terrain("forest");
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
        }

        Light light1 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);
        Light light2 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light2);

        start(20);

    }

    @Override
    public void turn() {
        super.turn();

    }
}
