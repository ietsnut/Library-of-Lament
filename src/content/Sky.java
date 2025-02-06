package content;

import game.Serial;
import object.Camera;
import property.Entity;
import property.Machine;
import resource.Mesh;

public class Sky extends Entity implements Machine {

    float modifier = (float) (Math.random());

    public Sky(String name, Mesh mesh) {
        super(mesh, name);
        this.rotation.x = (float) (Math.random() * 360);
        this.rotation.y = (float) (Math.random() * 360);
        this.rotation.z = (float) (Math.random() * 360);
        start(4);
    }

    @Override
    public void update() {
        this.model.buffer().identity().translate(Camera.position.x, Camera.position.y, Camera.position.z).scale(scale)
                .rotate((float) Math.toRadians(rotation.y), Y)
                .rotate((float) Math.toRadians(rotation.x), X)
                .rotate((float) Math.toRadians(rotation.z), Z);
        this.model.swap();
    }

    @Override
    public void turn() {
        rotation.x += modifier;
        rotation.y += modifier;
        rotation.z += modifier;
    }
}
