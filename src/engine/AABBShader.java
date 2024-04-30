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
        Entity entity = Entity.collides(30f, scene.entities);
        if (entity == null) {
            return;
        }
        uniform("model",        entity.model);
        uniform("projection",   Renderer.projection);
        uniform("view",         Camera.view);
        float t = Game.time();
        uniform("time",         t / 1000.0f);
        uniform("scale",        entity.collider.size);
        //render(entity.collider);
    }
}
