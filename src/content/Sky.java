package content;

import game.Serial;
import object.Camera;
import property.Entity;
import resource.Mesh;

public class Sky extends Entity {

    float speed = 1;

    public Sky(String name, Mesh mesh, float speed) {
        super(mesh, name);
        this.speed = speed;
    }

    @Override
    public void update() {
        this.model.buffer().identity().translate(Camera.position.x, Camera.position.y, Camera.position.z).scale(scale)
                .rotate((float) Math.toRadians((System.currentTimeMillis() % 1000000) / 1000.0f) * speed, Y)
                .rotate((float) Math.toRadians((System.currentTimeMillis() % 1000000) / 1000.0f) * speed, X)
                .rotate((float) Math.toRadians((System.currentTimeMillis() % 1000000) / 1000.0f) * speed, Z);
        this.model.swap();
    }

}
