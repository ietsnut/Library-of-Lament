package shader;

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
        entity.meshes[entity.state].bind();
        entity.materials[entity.state].bind();
        glDrawElements(GL_TRIANGLES, entity.meshes[entity.state].index, GL_UNSIGNED_INT, 0);
        entity.materials[entity.state].unbind();
        entity.meshes[entity.state].unbind();
    }

    @Override
    protected void shader(Scene scene) {
        uniform("projection",           Camera.projection.buffer());
        uniform("view",                 Camera.view.buffer());
        uniform("time",                 Manager.time());
        uniform("texture1",             0);
        for (Entity entity : scene.foreground) {
            if (entity.meshes[entity.state].linked() && entity.materials[entity.state].linked()) {
                render(entity);
            }
        }
    }
}
