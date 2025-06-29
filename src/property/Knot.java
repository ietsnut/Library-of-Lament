package property;

import engine.Console;
import resource.Mesh;

public class Knot extends Entity implements Interactive {

    public Knot(String name) {
        super(name, Mesh.knot(100, 20, 100, 0.5f));
    }

    @Override
    public void click() {
        Console.debug("clicked the knot!");
    }

    @Override
    public void enter() {

    }

    @Override
    public void leave() {

    }

}
