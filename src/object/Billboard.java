package object;

import org.joml.Matrix4f;

public class Billboard extends Entity {

    public Billboard(String name) {
        super(name, true);
        enqueue();
    }

    @Override
    public void load() {
        vertices = new float[] {
                -1.0f, 2.0f, 0,
                -1.0f, 0.0f, 0,
                1.0f, 0.0f, 0,
                1.0f, 2.0f, 0
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

    @Override
    public Matrix4f model() {
        rotation(Axis.Y, 360 - Camera.transformation.rotation.y);
        return super.model();
    }

}
