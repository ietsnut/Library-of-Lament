package scene;

import engine.Manager;
import entity.Real;
import engine.Scene;

import entity.Billboard;
import entity.Terrain;
import property.*;
import resource.Mesh;

public class Forest extends Scene implements Machine {

    private Entity sun1, sun2;
    private Entity tower;

    public Forest() {

        //terrain = new Terrain("dungeon");
        terrain = new Terrain("hills");

        tower = new Real("tower", 2);
        tower.scale.set(1/4f);
        tower.position.set(0, -0.3f, 0);
        tower.rotation.set(10, 0 ,5);
        background.add(tower);

        Billboard sword = new Billboard("sword");
        sword.position.set(13.8, 10.1, -36.0);
        sword.scale.set(8f);
        background.add(sword);

        Knot wall = new Knot("wall");
        wall.rotation.setComponent(0, -90);
        wall.position.set(0, 10, 0);
        wall.scale.set(0.1);
        wall.update();
        foreground.add(wall);

        sun1 = new Entity("sun1", Mesh.PLANE);
        sun1.position.y = 124;
        sun1.rotation.setComponent(0, 90);
        sun1.scale.set(48);
        foreground.add(sun1);

        sun2 = new Entity("sun2", Mesh.PLANE);
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
        tower.state = tower.state == 0 ? 1 : 0;
    }

}
