package scene;

import content.Real;
import engine.Scene;

import property.*;
import resource.Mesh;

public class Forest extends Scene {

    public Forest() {

        //terrain = new Terrain("dungeon");
        terrain = new Terrain("hills");

        Real real = new Real("tower");
        real.scale.set(1/8f);
        real.position.set(0, -0.3f, 0);
        real.rotation.set(10, 0 ,5);
        real.update();
        background.add(real);

        Entity tree = new Entity(Mesh.X_PLANE, "tree");
        tree.position.set(11, 5, -11);
        tree.scale.set(3);
        tree.update();
        foreground.add(tree);

    }

}
