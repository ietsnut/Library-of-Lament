package engine;

import game.Renderer;
import object.*;
import property.Entity;

import static org.lwjgl.opengl.GL40.*;

public class EntityShader extends Shader {

    public EntityShader() {
        super("entity", "position", "uv", "normal");
        start();
        uniform("texture1", 0);
        stop();
    }

    @Override
    public void shader() {
        uniform("vp", Camera.BUFFER);
        for (Entity entity : Renderer.ENTITIES) {
            if (entity.meshes[entity.state].binded()) {
                uniform("model", entity.buffer);
                glBindVertexArray(entity.meshes[entity.state].vao);
                for (byte i = 0; i < attributes.length; i++) {
                    glEnableVertexAttribArray(i);
                }
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, entity.materials[entity.state].texture);
                uniform("texture1", 0);
                glDrawElements(GL_TRIANGLES, entity.meshes[entity.state].index, GL_UNSIGNED_INT, 0);
                glBindTexture(GL_TEXTURE_2D, 0);
                for (int i = 0; i < attributes.length; i++) {
                    glDisableVertexAttribArray(i);
                }
                glBindVertexArray(0);
            }
        }
    }

}
