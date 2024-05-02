package engine;

import content.Vase;
import game.Control;
import game.Game;
import game.Scene;
import object.Camera;
import object.Entity;


public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        Entity entity = Entity.collides(30f, scene.entities);
        if (entity == null || !entity.bound() || !entity.collider.bound()) {
            return;
        }
        uniform("model",        entity.model);
        uniform("projection",   Renderer.projection);
        uniform("view",         Camera.view);
        uniform("time",         Game.time() / 1000.0f);
        uniform("scale",        entity.collider.size);
        render(entity.collider);
    }
}
