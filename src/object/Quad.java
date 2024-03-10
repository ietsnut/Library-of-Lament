package object;

import java.util.Arrays;

public class Quad extends Entity {

    public Texture texture;

    public Quad(String name) {
        super(name);
        texture = new Texture("resource/texture/" + name + ".png");
    }

    @Override
    protected void load(String name) {
        vertices = new float[] {
                -0.5f, 0.5f, 0,
                -0.5f, -0.5f, 0,
                0.5f, -0.5f, 0,
                0.5f, 0.5f, 0
        };
        indices = new int[] {
                0,1,3,
                3,1,2
        };
        texCoords = new float[] {
                0,0,
                0,1,
                1,1,
                1,0
        };
        normals = new float[]{
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f
        };
    }
}
