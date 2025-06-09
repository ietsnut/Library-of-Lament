package scene;

import content.Real;
import engine.Manager;
import engine.Scene;

import property.*;
import resource.Mesh;
import window.Main;

public class Forest extends Scene implements Machine {

    private Entity sun1, sun2;

    public Forest() {

        //terrain = new Terrain("dungeon");
        terrain = new Terrain("hills");

        Real real = new Real("tower");
        real.scale.set(1/4f);
        real.position.set(0, -0.3f, 0);
        real.rotation.set(10, 0 ,5);
        background.add(real);

        Billboard sword = new Billboard("sword");
        sword.position.set(13.8, 10.1, -36.0);
        sword.scale.set(8f);
        background.add(sword);

        sun1 = new Entity(Mesh.PLANE, "sun1");
        sun1.position.y = 124;
        sun1.rotation.setComponent(0, 90);
        sun1.scale.set(48);
        foreground.add(sun1);

        sun2 = new Entity(Mesh.PLANE, "sun2");
        sun2.position.y = 120;
        sun2.rotation.setComponent(0, 90);
        sun2.scale.set(48);
        foreground.add(sun2);

        start();

    }

    @Override
    public void turn() {
        sun1.rotation.y += 0.1f;
        sun2.rotation.y -= 0.1f;
    }

}
