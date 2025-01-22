package content;

import property.Entity;
import property.Machine;

public class Vase extends Entity implements Machine {

    public Vase(String name) {
        super(2);
        //start();
    }

    @Override
    public void turn() {
        this.rotation.y += 1;
        remodel();
    }

}
