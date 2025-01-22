package engine;

import game.Manager;
import game.Scene;
import object.Camera;
import property.Entity;

import static org.lwjgl.opengl.GL40.*;


public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        for (Entity entity : scene.entities) {
            if (entity.meshes[entity.mesh].collider == null || !entity.meshes[entity.mesh].collider.binded()) continue;
            render(entity);
        }
    }

    protected void render(Entity entity) {
        uniform("model",        entity.model);
        uniform("projection",   Camera.projection);
        uniform("view",         Camera.view);
        uniform("time",         Manager.time() / 1000.0f);
        uniform("scale",        entity.meshes[entity.mesh].collider.size);
        glBindVertexArray(entity.meshes[entity.mesh].collider.vao);
        for (byte i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        glDrawElements(GL_LINES, entity.meshes[entity.mesh].collider.indices.length, GL_UNSIGNED_INT, 0);
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

}
