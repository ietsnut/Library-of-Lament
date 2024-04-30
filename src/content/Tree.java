package content;

import object.Entity;

public class Tree extends Entity {

    public Tree(String model) {
        super(model, true);
        enqueue();
    }

    @Override
    public void load() {
        vertices = new byte[] {
                // First Quad (One diagonal of the X)
                (byte) -1, 0, 0, // Lower left
                (byte) 1, 0, 0,  // Lower right
                (byte) -1, (byte) 2, 0, // Upper left
                (byte) 1, (byte) 2, 0,  // Upper right
                0, 0, (byte) -1, // Lower left
                0, 0, (byte) 1,  // Lower right
                0, (byte) 2, (byte) -1, // Upper left
                0, (byte) 2, (byte) 1   // Upper right
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
                0f, 0f, 1f,  // Points out of the screen
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,
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
