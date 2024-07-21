package content;

import property.Entity;
import property.Machine;

public class Vase extends Entity implements Machine {

    long shake;

    public Vase(byte id) {
        super(id);
    }

    @Override
    public void process() {
        this.rotation.y += 1;
        remodel();
    }
}
