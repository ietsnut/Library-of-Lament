package content;

import game.Serial;
import property.Entity;
import property.Interactive;
import property.Machine;

public class House extends Entity implements Interactive {

    public House(String name) {
        super(name);
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
