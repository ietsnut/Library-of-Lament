package shader;

import content.Sky;
import engine.Console;
import engine.Manager;
import property.Light;
import engine.Scene;
import object.*;
import org.joml.Vector3f;
import property.Entity;

import java.util.List;

import static org.lwjgl.opengl.GL40.*;

public class EntityShader extends Shader<Scene> {

    public EntityShader() {
        super("entity", "position", "uv", "normal");
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
        uniform("projection",           Camera.projection.buffer());
        uniform("view",                 Camera.view.buffer());
        uniform("lightPosition[0]",     Camera.position);
        //uniform("lightAttenuation[0]",  new Vector3f(2.0f, 0.7f, 0.07f));
        uniform("lightAttenuation[0]", new Vector3f(1.0f, 0.2f, 0.02f));
        uniform("lightIntensity[0]",   1.5f);
        int LIGHT = 1;
        for (Light light : scene.lights) {
            uniform("lightPosition[" + LIGHT + "]",     light.position);
            uniform("lightAttenuation[" + LIGHT + "]",  light.attenuation);
            uniform("lightIntensity[" + LIGHT + "]",    light.intensity);
            LIGHT++;
        }
        uniform("lights", LIGHT);
        uniform("illumination", 0f);
        uniform("texture1", 0);
        for (Entity entity : scene.entities) {
            if (entity.meshes[entity.state].binded()) {
                if (entity instanceof Sky) {
                    uniform("illumination", 2f);
                }
                render(entity);
                uniform("illumination", 0f);
            }
        }
        if (scene.terrain != null && scene.terrain.meshes[scene.terrain.state].binded() && scene.terrain.materials[scene.terrain.state].binded()) {
            render(scene.terrain);
        }
    }
}
