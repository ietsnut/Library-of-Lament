package content;

import object.Camera;
import org.joml.Vector3f;
import property.Entity;
import property.Interactive;

public class Door extends Entity implements Interactive {

    public Door(String name) {
        super(name);
    }

    @Override
    public void click() {
        Camera.moveTo(new Vector3f(0, 1.7f, 0));
    }

    @Override
    public void enter() {
        Camera.moveTo(new Vector3f(0, 1.7f, 0));
    }

    @Override
    public void leave() {
    }

}
