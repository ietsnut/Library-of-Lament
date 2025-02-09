package scene;

import content.Character;
import content.Plant;
import content.Sky;
import game.Scene;
import property.Cylinder;
import property.Entity;
import property.Light;
import org.joml.Vector3f;
import property.Terrain;
import resource.Mesh;

import java.util.Random;

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


        int numberOfPlants = 100; // for example, 100 plants

        int terrainWidth = 100;
        // Create a random number generator
        Random random = new Random();

        // Loop to create and position each plant
        for (int i = 0; i < numberOfPlants; i++) {
            // Generate random x and z coordinates within the terrain bounds
            int x = random.nextInt(terrainWidth) - (terrainWidth / 2);
            int z = random.nextInt(terrainWidth) - (terrainWidth / 2);

            // Create a new plant (assuming '4' is a parameter such as a texture index or model type)
            Plant plant = new Plant(4);

            // Set the plant's position to the generated coordinates
            plant.position.x = x;
            plant.position.z = z;

            plant.scale.set(random.nextInt(1, 4));

            // Add the plant to your list of entities (or game objects)
            entities.add(plant);
        }

        new Thread(() -> {

            while (!terrain.meshes[0].binded()) {
                try {
                    Thread.sleep(10); // sleep a short time to avoid busy-waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (Entity entity : entities) {
                entity.position.y = terrain.height(entity.position.x, entity.position.z) - 1f;
            }

        }).start();


        /*
        Light light1 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);
        Light light2 = new Light(new Vector3f(0, 2, 0), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light2);
        */

    }

}
