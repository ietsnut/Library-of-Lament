package shader;

import engine.Console;
import engine.Manager;
import engine.Scene;
import object.*;
import property.Entity;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class EntityShader extends Shader<Scene> {

    public EntityShader(Window window) {
        super(window, "entity", "position", "uv");
    }

    private void render(Entity entity) {
        uniform("model", entity.model.buffer());
        uniform("state", entity.state);
        uniform("states", entity.states);
        entity.mesh.bind();
        entity.material.bind();
        glDrawElements(GL_TRIANGLES, entity.mesh.index, GL_UNSIGNED_INT, 0);
        entity.material.unbind();
        entity.mesh.unbind();
    }

    @Override
    protected void shader(Scene scene) {
        uniform("projection",           Camera.projection.buffer());
        uniform("view",                 Camera.view.buffer());
        uniform("time",                 Manager.time());
        for (Entity entity : scene.foreground) {
            if (entity.mesh.linked() && entity.material.linked()) {
                render(entity);
            }
        }
    }
}