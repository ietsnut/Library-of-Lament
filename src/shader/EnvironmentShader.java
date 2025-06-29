package shader;

import engine.Console;
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
        glEnable(GL_DEPTH_TEST);
        uniform("projection", Camera.projection.buffer());
        uniform("view", Camera.view.buffer());
        uniform("fogDensity", 0.02f);
        uniform("fogGradient", 1.5f);
        uniform("texture1", 0);
        for (Entity entity : scene.background) {
            if (entity.mesh.linked() && entity.material.linked()) {
                render(entity);
            }
        }
        if (scene.terrain != null && scene.terrain.mesh.linked() && scene.terrain.material.linked()) {
            render(scene.terrain);
        }
    }
}