package content;

import object.Entity;

public class Tree extends Entity {

    public Tree(String model) {
        super(model, true);
        enqueue();
    }

    @Override
    public void load() {
        vertices = new float[] {
                // First Quad (One diagonal of the X)
                -1.0f, 0.0f, 0.0f, // Lower left
                1.0f, 0.0f, 0.0f,  // Lower right
                -1.0f, 2.0f, 0.0f, // Upper left
                1.0f, 2.0f, 0.0f,  // Upper right

                // Second Quad (The other diagonal of the X)
                0.0f, 0.0f, -1.0f, // Lower left
                0.0f, 0.0f, 1.0f,  // Lower right
                0.0f, 2.0f, -1.0f, // Upper left
                0.0f, 2.0f, 1.0f   // Upper right
        };
        texCoords = new float[] {
                0, 1,
                1, 1,
                0, 0,
                1, 0,
                0, 1,
                1, 1,
                0, 0,
                1, 0
        };
        normals = new float[] {
                // Normals for the first quad
                0f, 0f, 1f,  // Points out of the screen
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,

                // Normals for the second quad
                1f, 0f, 0f,  // Points right
                1f, 0f, 0f,
                1f, 0f, 0f,
                1f, 0f, 0f
        };
        indices = new int[] {
                0, 2, 1, // First Triangle of the first quad
                1, 2, 3, // Second Triangle of the first quad
                4, 6, 5, // First Triangle of the second quad
                5, 6, 7  // Second Triangle of the second quad
        };
    }


}
