package content;

import game.Game;
import object.Model;
import org.joml.Matrix4f;
import property.Interactive;
import property.Load;

import java.util.BitSet;

public class Vase extends Model implements Interactive {

    boolean reload = false;

    public Vase(String model) {
        super("vase", model, true);
        this.states = new boolean[1];
        queue();
    }

    @Override
    protected Matrix4f model() {
        if (!this.states[0]) {
            rotation.y = Game.TIME / 1000.0f;
        }
        return super.model();
    }

    @Override
    public void interact() {
        if (!this.states[0]) {
            this.states[0] = true;
            this.name = "vase_broken";
            reload = true;
        }
    }

    @Override
    public boolean reload() {
        if (reload) {
            reload = false;
            return true;
        }
        return super.reload();
    }

}
