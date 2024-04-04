package engine;

import game.Scene;
import object.Billboard;
import object.Entity;
import object.Model;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        for (Entity entity : scene.entities) {
            uniform("model", entity.transformation.model());
            uniform("projection", Renderer.projection());
            uniform("view", Renderer.camera.transformation.view());
            uniform("selected", entity.aabb.selected);
            render(entity.aabb);
        }
    }
}
