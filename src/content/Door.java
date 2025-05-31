package content;

import object.Camera;
import property.Entity;
import property.Interactive;

public class Door extends Entity implements Interactive {

    public Door(String name) {
        super(name);
    }

    @Override
    public void click() {
        System.out.println("Clicked");
        Camera.reset();
    }

    @Override
    public void enter() {
        System.out.println("Entered");
        Camera.reset();
    }

    @Override
    public void leave() {
        System.out.println("Left");
    }

}
