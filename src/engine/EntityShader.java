package engine;

import property.Light;
import game.Scene;
import object.*;
import org.joml.Vector3f;
import property.Entity;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class EntityShader extends Shader {

    public EntityShader() {
        super("entity", "position", "uv", "normal");
    }

    public void shader(Scene scene) {
        uniform("projection",       Camera.projection);
        uniform("view",             Camera.view);
        uniform("lightPosition[0]",     Camera.position);
        uniform("lightAttenuation[0]",  new Vector3f(1.0f, 0.7f, 0.07f));
        uniform("lightIntensity[0]",    2f);
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