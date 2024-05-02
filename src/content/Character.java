package content;

import object.Camera;
import object.Entity;
import org.joml.Matrix4f;

public class Character extends Entity {

    public Character(String name) {
        super("character", name, true);
        queue();
    }

    @Override
    public void load() {
        vertices = new byte[] {
                (byte) -1, (byte) 2, 0,
                (byte) -1, 0, 0,
                (byte) 1, 0, 0,
                (byte) 1, (byte) 2, 0
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
    protected Matrix4f model() {
        rotation.y = Camera.transformation.rotation.y;
        return super.model();
    }


}
