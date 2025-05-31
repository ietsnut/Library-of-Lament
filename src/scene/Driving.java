package scene;

import content.Door;
import content.Train;
import content.Vase;
import engine.Manager;
import engine.Scene;
import org.joml.Vector3f;
import property.Entity;
import property.Light;
import property.Machine;
import property.Terrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driving extends Scene implements Machine {

    private final List<Entity> forests = new ArrayList<>();
    private final int forestCount = 3;
    private float time = 0;

    private final Map<Entity, Vector3f> basePositions = new HashMap<>();

    public Driving() {

        terrain = new Terrain("train_inside");
        terrain.update();

        basePositions.put(terrain, new Vector3f(terrain.position));

        /*
        Train train = new Train("train_inside");
        train.position.set(0, 0, 0.5f);
        train.update();
        entities.add(train);

        basePositions.put(train, new Vector3f(train.position));

         */

        Vase entity2 = new Vase("2");
        entity2.scale.set(1 / 64f);
        entity2.position.set(-0.5f, 1, -1.5f);
        entity2.rotation.set(0, -45, 0);
        entity2.update();
        entities.add(entity2);

        basePositions.put(entity2, new Vector3f(entity2.position));

        Door door = new Door("train");
        door.scale.set(0.8f);
        door.position.set(0, 0, 0.2f);
        door.update();
        entities.add(door);

        basePositions.put(door, new Vector3f(door.position));

        // Create multiple forest tiles spaced in the Z-direction
        for (int i = 0; i < forestCount; i++) {
            Entity forest = new Entity("forest");
            forest.position.set(0, -10, -i * 220f);  // Going backwards in z
            forest.update();
            entities.add(forest);
            forests.add(forest);
        }

        Light light1 = new Light(new Vector3f(0, 1.6f, -1.6f), new Vector3f(2.0f, 0.7f, 0.07f), 1f);
        lights.add(light1);

        start(Manager.RATE);
    }

    @Override
    public void turn() {
        time += 0.125f;

        // Wobble all entities relative to base positions
        float wobbleFrequencyX = 0.4f; // Side-to-side
        float wobbleFrequencyY = 0.9f; // Up and down
        float wobbleAmplitudeX = 0.02f; // Bigger wobble horizontally
        float wobbleAmplitudeY = 0.01f; // Bigger wobble vertically

        float wobbleX = (float) Math.sin(time * wobbleFrequencyX) * wobbleAmplitudeX;
        float wobbleY = (float) Math.sin(time * wobbleFrequencyY + 1) * wobbleAmplitudeY;

        for (Entity e : entities) {
            Vector3f basePos = basePositions.get(e);
            if (basePos != null) {
                e.position.x = basePos.x + wobbleX;
                e.position.y = basePos.y + wobbleY;
            }
        }

        Vector3f basePos = basePositions.get(terrain);
        if (basePos != null) {
            terrain.position.x = basePos.x + wobbleX;
            terrain.position.y = basePos.y + wobbleY;
            terrain.update();
        }

        // Move forests and loop them
        final float resetZ = 122f; // Once past the camera
        for (Entity forest : forests) {
            forest.position.z += 1f; // Move toward the camera (positive z)

            if (forest.position.z > resetZ) {
                // Reposition behind the farthest forest
                float minZ = forests.stream()
                        .map(f -> f.position.z)
                        .min(Float::compare)
                        .orElse(0f);
                forest.position.z = minZ - 220f;
            }

            forest.update();
        }
    }

}
