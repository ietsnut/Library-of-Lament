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
        System.out.println("Clicked");
        Camera.moveTo(new Vector3f(0, 1.7f, 0));
    }

    @Override
    public void enter() {
        System.out.println("Entered");
        Camera.moveTo(new Vector3f(0, 1.7f, 0));
    }

    @Override
    public void leave() {
        System.out.println("Left");
    }

}
