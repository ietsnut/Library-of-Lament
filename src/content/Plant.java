package content;

import game.Serial;
import property.Entity;
import property.Machine;
import resource.Mesh;

public class Plant extends Entity implements Machine {

    public Plant(int states) {
        super(states, Mesh.X_SHAPE);
        start(8);
    }

    @Override
    public void turn() {
        this.state = Math.clamp((Serial.states[1] / 4), 0, 3);
    }

}
