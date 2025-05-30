package content;

import object.Camera;
import property.Entity;
import property.Interactive;
import property.Machine;

public class Portal extends Entity implements Interactive {

    public Portal(String name) {
        super(name);
    }

    @Override
    public void click() {
        System.out.println("Clicked");
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
