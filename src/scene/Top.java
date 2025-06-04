package scene;

import content.Vase;
import engine.Scene;

public class Top extends Scene {

    public Top() {

        Vase vase = new Vase("2");
        vase.scale.set(1/16f);
        vase.position.set(1, 1, 1);
        vase.rotation.set(0, -45 ,0);
        entities.add(vase);

    }

}
