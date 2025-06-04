package scene;

import content.Real;
import engine.Scene;
import org.joml.Vector3f;
import property.Entity;
import property.Light;
import property.Terrain;
import resource.Mesh;

import java.util.Random;

public class Test extends Scene {

    public Test() {

        terrain = new Terrain("forest");
        terrain.update();

        Real real = new Real("tower");
        real.scale.set(1/8f);
        real.position.set(0, -0.3f, 0);
        real.rotation.set(10, 0 ,5);
        real.update();
        entities.add(real);

        Random rand = new Random();
        int numTrees = 50; // Adjust this number as needed

        for (int i = 0; i < numTrees; i++) {
            Entity scatteredTree = new Entity(Mesh.X_PLANE, "tree");

            // Random position within the 40x40 map (-20 to 20 on both axes)
            float x = rand.nextFloat() * 40 - 20;
            float z = rand.nextFloat() * 40 - 20;

            // Avoid placing trees too close to the tower (within 3 units)
            float distanceToTower = (float) Math.sqrt(x * x + z * z);
            if (distanceToTower < 3) {
                i--; // Try again
                continue;
            }

            scatteredTree.position.set(x, 0, z);

            // Vary the scale slightly for more natural look
            float scale = 2.5f + rand.nextFloat() * 1.0f; // Scale between 2.5 and 3.5
            scatteredTree.scale.set(scale);

            // Optional: slight random rotation for variety
            float rotation = rand.nextFloat() * 360;
            scatteredTree.rotation.set(0, rotation, 0);
            scatteredTree.update();
            entities.add(scatteredTree);
        }
        Light light1 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.2f, 0.02f), 2f);
        lights.add(light1);
    }


}
