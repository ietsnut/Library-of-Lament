package engine;

import game.Manager;
import game.Scene;
import game.Serial;
import property.Entity;
import object.FBO;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader {

    public FBOShader() {
        super("fbo", "position");
        FBO.load();
        start();
        uniform("texture1", 1);
        uniform("texture2", 2);
        uniform("texture3", 3);
        stop();
    }

    public void shader(Scene scene) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        render();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    private void render() {
        glBindVertexArray(1);
        glEnableVertexAttribArray(0);
        for (int i = 1; i < 4; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, i);
        }
        glDrawElements(GL_TRIANGLES, FBO.INDICES.length, GL_UNSIGNED_INT, 0);
        for (int i = 1; i < 4; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, FBO.ID);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
    }

}
