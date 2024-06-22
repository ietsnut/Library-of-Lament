package engine;

import game.Game;
import game.Scene;
import property.Entity;
import object.FBO;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader {

    public static FBO fbo;

    public FBOShader() {
        super("fbo", "position");
        fbo = new FBO();
    }

    public void shader(Scene scene) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        render(fbo);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    @Override
    protected void render(Entity entity) {
        glBindVertexArray(fbo.meshes.get(fbo.mesh).vao);
        for (int i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        for (int i = 0; i < entity.materials.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, entity.materials.get(i).texture);
            uniform("texture" + (i + 1), i);
        }
        glDrawElements(GL_TRIANGLES, fbo.meshes.getFirst().indices.length, GL_UNSIGNED_INT, 0);
        for (int i = 0; i < entity.materials.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.id);
        glViewport(0, 0, Game.WIDTH, Game.HEIGHT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Set clear color to opaque black
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearDepth(1.0f);  // Clearing depth to the farthest
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Game.WIDTH, Game.HEIGHT);
    }

}
