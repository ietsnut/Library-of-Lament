package content;

import object.Entity;
import property.Interactive;

public class Vase extends Entity implements Interactive {

    long shake;

    public Vase(byte id) {
        super(id);
    }

    @Override
    public void update() {

    }

    @Override
    public void onClick() {

    }

    @Override
    public void onHold() {
        System.out.println("Holding Vase");
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onExit() {

    }

}
