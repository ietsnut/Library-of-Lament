package shader;

import engine.Manager;
import engine.Scene;
import object.*;
import property.Entity;

import static org.lwjgl.opengl.GL40.*;

public class EntityShader extends Shader<Scene> {

    public EntityShader() {
        super("entity", "position", "uv");
    }


    private void render(Entity entity) {
        uniform("model", entity.model.buffer());
        glBindVertexArray(entity.meshes[entity.state].vao);
        for (byte i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, entity.materials[entity.state].id);
        glDrawElements(GL_TRIANGLES, entity.meshes[entity.state].index, GL_UNSIGNED_INT, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

    @Override
    protected void shader(Scene scene) {
        uniform("projection",           Camera.projection.buffer());
        uniform("view",                 Camera.view.buffer());
        uniform("texture1", 0);
        uniform("time",                 Manager.time());
        for (Entity entity : scene.entities) {
            if (entity.meshes[entity.state].binded() && entity.materials[entity.state].binded()) {
                render(entity);
            }
        }
        if (scene.terrain != null && scene.terrain.meshes[scene.terrain.state].binded() && scene.terrain.materials[scene.terrain.state].binded()) {
            render(scene.terrain);
        }
    }
}
