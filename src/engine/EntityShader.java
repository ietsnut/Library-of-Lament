package engine;

import content.Sky;
import property.Light;
import game.Scene;
import object.*;
import org.joml.Vector3f;
import property.Entity;

import static org.lwjgl.opengl.GL40.*;

public class EntityShader extends Shader {

    public EntityShader() {
        super("entity", "position", "uv", "normal");
        start();
        uniform("texture1", 0);
        stop();
    }

    public void shader(Scene scene) {
        uniform("projection",           Camera.projection.get());
        uniform("view",                 Camera.view.get());
        uniform("illumination", 0f);
        uniform("texture1", 0);
        for (Entity entity : scene.entities) {
            if (entity.meshes[entity.state].binded()) {
                if (entity instanceof Sky) {
                    uniform("illumination", 0.5f);
                }
                render(entity);
                uniform("illumination", 0f);
            }
        }
        if (scene.terrain.meshes[scene.terrain.state].binded()) {
            render(scene.terrain);
        }
    }

    protected void render(Entity entity) {
        uniform("model", entity.model.get());
        glBindVertexArray(entity.meshes[entity.state].vao);
        for (byte i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, entity.materials[entity.state].texture);
        glDrawElements(GL_TRIANGLES, entity.meshes[entity.state].index, GL_UNSIGNED_INT, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

}
