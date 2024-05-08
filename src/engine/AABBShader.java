package engine;

import game.Game;
import game.Scene;
import object.Camera;
import object.Entity;


public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        /*
        Entity active = Entity.lookingAt(scene.entities, 30f);
        if (active == null) {
            return;
        }
        uniform("model", active.model);
        uniform("projection", Camera.projection);
        uniform("view", Camera.view);
        uniform("time", Game.time() / 1000.0f);
        uniform("scale", active.collider.size);
        render(active.collider);

         */
    }

    @Override
    protected void render(Entity entity) {

    }
}
