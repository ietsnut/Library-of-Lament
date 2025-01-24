package content;

import game.Serial;
import property.Entity;
import property.Machine;

public class Plant extends Entity implements Machine {

    public Plant(String name) {
        super(3);
        start();
    }

    @Override
    public void turn() {
        this.state = Serial.states[0];
    }

}