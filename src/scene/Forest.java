package scene;

import content.Door;
import content.Real;
import content.Vase;
import engine.Scene;
import org.joml.Vector3f;
import property.*;

public class Forest extends Scene {

    public Forest() {

        terrain = new Terrain("dungeon");

        Real real = new Real("tower");
        real.scale.set(1/8f);
        real.position.set(0, -0.3f, 0);
        real.rotation.set(10, 0 ,5);
        real.update();
        entities.add(real);

    }

}
