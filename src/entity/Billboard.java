package entity;


import object.Camera;
import property.Entity;
import resource.Mesh;
import org.joml.Vector3f;

public class Billboard extends Entity {

    public Billboard(String name) {
        super(name, 1, Mesh.PLANE);
    }

    @Override
    public void update() {
        Vector3f direction = new Vector3f(Camera.position).sub(position);
        float angleY = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
        rotation.set(0, angleY, 0);
        this.model.inactive().identity()
                .translate(position.x, position.y, position.z)
                .scale(scale)
                .rotate((float) Math.toRadians(rotation.y), Y)
                .rotate((float) Math.toRadians(rotation.x), X)
                .rotate((float) Math.toRadians(rotation.z), Z);
        this.model.swap();
    }

}
