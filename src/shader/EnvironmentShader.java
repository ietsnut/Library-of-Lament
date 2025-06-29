package shader;

import engine.Scene;
import object.Camera;
import property.Entity;
import window.Window;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class EnvironmentShader extends Shader<Scene> {

    public EnvironmentShader(Window window) {
        super(window, "env", "position", "uv");
        start();
        uniform("texture1", 0);
        stop();
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
        glEnable(GL_DEPTH_TEST);
        uniform("projection", Camera.projection.buffer());
        uniform("view", Camera.view.buffer());
        // uniform("fogDensity", 0.04f);
        // uniform("fogGradient", 1.5f);
        uniform("fogDensity", 0.02f);
        uniform("fogGradient", 1.5f);
        uniform("texture1", 0);
        for (Entity entity : scene.background) {
            if (entity.meshes[entity.state].linked() && entity.materials[entity.state].linked()) {
                render(entity);
            }
        }
        if (scene.terrain != null && scene.terrain.meshes[scene.terrain.state].linked()
                && scene.terrain.materials[scene.terrain.state].linked()) {
            render(scene.terrain);
        }
    }
}
