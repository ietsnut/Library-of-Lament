package content;

import engine.Serial;
import property.Entity;
import property.Interactive;
import property.Machine;
import property.Solid;

public class Vase extends Entity implements Machine, Interactive {

    public Vase(String name) {
        super(name);
        start();
    }

    @Override
    public void turn() {
        this.state = Serial.states[0];
        //this.rotation.y += 1;
    }

    @Override
    public void click() {
        System.out.println("Clicked");
    }

    @Override
    public void enter() {
        System.out.println("Entered");
    }

    @Override
    public void leave() {
        System.out.println("Left");
    }

}
