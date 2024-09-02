package content;

import game.Scene;
import component.Light;
import org.joml.Vector3f;
import component.Machine;
import property.Terrain;

public class Sewer extends Scene implements Machine {

    public Sewer() {
        terrain = new Terrain("sewer");
        Vase lightholder = new Vase("0");
        lightholder.position.set(0, 2, 0);
        lightholder.add(new Light(new Vector3f(1.0f, 0.7f, 0.07f), 2f));
        entities.add(lightholder);
        for (int i = 0; i < 30; i+= 5) {
            Vase vase = new Vase("0");
            vase.scale.set(1/16f);
            float x = (float) (Math.random() * i);
            float z = 1.5f;
            if (Math.random() > 0.5) {
                x = -x;
            }
            if (Math.random() > 0.5) {
                z = -z;
            }
            vase.position.set(x,  0, z);
            vase.remodel();
            entities.add(vase);
        }
        /*
        Light light1 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        vase.components.add(light1);
        Light light2 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light2);

         */
        start(1);
    }

    @Override
    public void turn() {
        System.out.println("Sewer");
    }

}
