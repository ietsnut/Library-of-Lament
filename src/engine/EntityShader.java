package engine;

import component.Light;
import game.Scene;
import object.*;
import property.Entity;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class EntityShader extends Shader {

    private static int LIGHT = 0;

    public EntityShader() {
        super("entity", "position", "uv", "normal");
    }

    public void shader(Scene scene) {
        uniform("projection",       Camera.projection);
        uniform("view",             Camera.view);
        LIGHT = 1;
        for (Entity entity : scene.entities) {
            List<Light> lights = entity.components(Light.class);
            if (lights == null) continue;
            for (Light light : lights) {
                uniform("lightPosition[" + LIGHT + "]",     entity.position);
                uniform("lightAttenuation[" + LIGHT + "]",  light.attenuation);
                uniform("lightIntensity[" + LIGHT + "]",    light.intensity);
                LIGHT++;
            }
        }
        uniform("lights", LIGHT);
        uniform("texture1", 0);
        for (Entity entity : scene.entities) {
            if (entity.meshes[entity.mesh].binded()) {
                render(entity);
            }
        }
        if (scene.terrain.meshes[scene.terrain.mesh].binded()) {
            render(scene.terrain);
        }
    }

    protected void render(Entity entity) {
        uniform("model", entity.model);
        glBindVertexArray(entity.meshes[entity.mesh].vao);
        for (byte i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, entity.materials[entity.material].texture);
        glDrawElements(GL_TRIANGLES, entity.meshes[entity.mesh].indices.length, GL_UNSIGNED_INT, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

}
