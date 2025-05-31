package content;

import engine.Console;
import engine.Serial;
import object.Camera;
import property.Entity;
import property.Interactive;
import property.Machine;
import property.Solid;

public class Vase extends Entity implements Interactive {

    public Vase(String name) {
        super(name);
    }

    @Override
    public void click() {
        Console.debug("charlie is a stinker");
    }

    @Override
    public void enter() {

    }

    @Override
    public void leave() {

    }
}
