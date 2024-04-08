package object;

import engine.Renderer;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import property.Transformation;

public class Billboard extends Entity {

    public Billboard(String name) {
        super(name);

        this.transformation = new Transformation() {
            @Override
            public Matrix4f model() {
                Matrix4f matrix = super.model();
                matrix.setIdentity();
                matrix.translate(position);
                Vector3f directionToCamera = Vector3f.sub(Camera.transformation.position, position, null);
                if (directionToCamera.lengthSquared() > 0) {
                    directionToCamera.normalise();
                    matrix.rotate((float) Math.atan2(directionToCamera.x, directionToCamera.z), AXIS_Y);
                }
                matrix.scale(new Vector3f(scale.x, scale.y, scale.z));
                return matrix;
            }
        };
        texture("texture", name);
    }

    @Override
    protected void load(Object... args) {
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
