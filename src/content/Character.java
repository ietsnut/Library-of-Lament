package content;

import property.Entity;
import property.Interactive;

public class Character extends Entity implements Interactive {

    public Character(String name) {
        super(name);
    }

    @Override
    public void click() {
        System.out.println("clicked");
    }

    @Override
    public void enter() {

    }

    @Override
    public void leave() {

    }
}
