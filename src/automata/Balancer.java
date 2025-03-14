package automata;

import engine.Serial;
import object.Automata;

public class Balancer extends Automata {

    public Balancer() {
        super("left", "right");
        scale.set(8);
        update();
    }


    @Override
    public void update(Component component) {
        if (component.id.equalsIgnoreCase("right")) {
            int in = (Serial.IN[0] + Serial.IN[1]) - Serial.IN[0];
            component.position.y = in + 0.1f;
        }
        if (component.id.equalsIgnoreCase("left")) {
            int in = (Serial.IN[0] + Serial.IN[1]) - Serial.IN[1];
            component.position.y = in + 0.1f;
        }
    }

}
