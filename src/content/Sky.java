package content;

import game.Serial;
import object.Camera;
import property.Entity;
import property.Machine;

public class Sky extends Entity implements Machine {

    public Sky() {
        super(1);
        start();
    }

    @Override
    public void turn() {
        this.position.set(Camera.position);
    }

}
