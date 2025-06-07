package scene;

import content.Real;
import engine.Scene;
import property.Entity;
import property.Terrain;
import resource.Mesh;

import java.util.Random;

public class Test extends Scene {

    public Test() {

        Entity tree = new Entity(Mesh.X_PLANE, "tree");
        tree.position.set(11, 5, -11);
        tree.scale.set(3);
        entities.add(tree);

    }


}
