package object;

import org.lwjgl.Sys;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Decal extends Billboard {

    public Decal(String name) {
        super(name);
    }


    @Override
    public Matrix4f getModelMatrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.translate(position);

        Vector3f directionToCamera = Vector3f.sub(Camera.position, position, null);
        if (directionToCamera.lengthSquared() > 0) {
            directionToCamera.normalise();
            matrix.rotate((float) Math.atan2(directionToCamera.x, directionToCamera.z), AXIS_Y);
        }

        matrix.scale(new Vector3f(scale.x, scale.y, scale.z));
        return matrix;
    }

}
