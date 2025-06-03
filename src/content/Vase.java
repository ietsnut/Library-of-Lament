package content;

import engine.Console;
import property.Entity;
import property.Interactive;

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
