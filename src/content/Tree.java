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
                -1.0f, 2.0f, 0,
                -1.0f, 0.0f, 0,
                1.0f, 0.0f, 0,
                1.0f, 2.0f, 0,
                0.0f, 2.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 2.0f, 1.0f
        };
        texCoords = new float[] {
                0, 0,
                0, 1,
                1, 1,
                1, 0,
                0, 0,
                0, 1,
                1, 1,
                1, 0
        };
        normals = new float[] {
                0f, 0f, -1f,
                1f, 0f, 0f
        };
        indices = new int[] {
                0, 1, 3,
                3, 1, 2,
                4, 5, 7,
                7, 5, 6
        };
    }

}
