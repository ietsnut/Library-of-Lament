package engine;

import game.Scene;
import object.Billboard;
import object.Camera;
import object.Entity;
import object.Model;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        Entity entity = Entity.collides(30f, scene.entities);
        if (entity == null) {
            return;
        }
        System.out.println(entity.distance());
        uniform("model",        entity.transformation.model());
        uniform("projection",   Renderer.projection());
        uniform("view",         Camera.view);
        float t = (Sys.getTime() * 1000.0f) / Sys.getTimerResolution();
        uniform("time",         t / 1000.0f);
        render(entity.collider);
        /*
        for (Entity entity : scene.entities) {
            if (entity.collides(30f)) {
                System.out.println(entity.distance());
                uniform("model",        entity.transformation.model());
                uniform("projection",   Renderer.projection());
                uniform("view",         Camera.view);
                float t = (Sys.getTime() * 1000.0f) / Sys.getTimerResolution();
                uniform("time",         t / 1000.0f);
                render(entity.collider);
            }
        }*/
    }
}
