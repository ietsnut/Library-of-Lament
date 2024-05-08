package engine;

import content.Terrain;
import game.Scene;
import org.joml.Math;
import object.*;
import property.Material;
import property.Mesh;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class ModelShader extends Shader {

    public ModelShader() {
        super("model", "position", "uv", "normal");
    }

    public void shader(Scene scene) {
        uniform("projection",       Camera.projection);
        uniform("view",             Camera.view);
        uniform("lights",           Math.min(scene.lights.size(), LIGHTS));
        for (byte i = 0; i < Math.min(scene.lights.size(), LIGHTS); i++) {
            uniform("lightPosition[" + i + "]",     scene.lights.get(i).position);
            uniform("lightAttenuation[" + i + "]",  scene.lights.get(i).attenuation);
            uniform("lightIntensity[" + i + "]",    scene.lights.get(i).intensity);
        }
        for (Entity entity : scene.entities) {
            if (entity.meshes.get(entity.mesh).bound) {
                render(entity);
            }
        }
    }

    @Override
    protected void render(Entity entity) {
        uniform("model",        entity.model);
        glBindVertexArray(entity.meshes.get(entity.mesh).vao);
        for (int i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, entity.materials.get(entity.material).texture);
        uniform("texture1", 0);
        glDrawElements(GL_TRIANGLES, entity.meshes.get(entity.mesh).indices.length, GL_UNSIGNED_INT, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

}
