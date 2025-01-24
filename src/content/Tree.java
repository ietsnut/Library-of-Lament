package content;

import game.Serial;
import property.Entity;
import property.Machine;

public class Tree extends Entity implements Machine {

    public Tree(String name) {
        super(6);
        start();
    }

    @Override
    public void turn() {
        this.state = Serial.states[1];
    }

}
