package content;

import game.Game;
import object.Model;
import org.joml.Matrix4f;
import property.Interactive;

public class Vase extends Model implements Interactive {

    long shake;

    public Vase(String model) {
        super(model, 3);
        queue();
    }

    @Override
    protected Matrix4f model() {
        if (state.equals(0)) {
            rotation.y = Game.TIME / 1000.0f;
        } else if (state.equals(1)) {
            float elapsed = Game.TIME - shake;
            if (elapsed < 500) {
                rotation.x += (float) Math.sin(elapsed);
                rotation.z += (float) Math.cos(elapsed);
            } else {
                rotation.x = 0;
                rotation.z = 0;
                state.set(0);
            }
        }
        return super.model();
    }

    @Override
    public void onClick() {

    }

    @Override
    public void onHold() {
        if (state.equals(0)) {
            if (Math.random() < 0.8) {
                shake = Game.TIME;
                state.set(1);
            } else {
                state.set(2);
                this.name = "vase_broken";
                textures.getFirst().name = "vase_broken";
            }
        }
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onExit() {

    }

}
