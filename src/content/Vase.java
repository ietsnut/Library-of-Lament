package content;

import property.Entity;
import property.Interactive;
import property.Machine;

public class Vase extends Entity implements Interactive, Machine {

    long shake;

    public Vase(byte id) {
        super(id);
    }

    @Override
    public void click() {

    }

    @Override
    public void hold() {

    }

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public void process() {
        this.rotation.y += 1;
        remodel();
    }
}
