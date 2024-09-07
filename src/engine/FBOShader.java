package engine;

import game.Manager;
import game.Scene;
import property.Entity;
import object.FBO;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader {

    public FBOShader() {
        super("fbo", "position");
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
        glBindVertexArray(FBO.MESH.vao);
        for (int i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        for (int i = 0; i < FBO.MATERIALS.length; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, FBO.MATERIALS[i].texture);
            uniform("texture" + (i + 1), i);
        }
        glDrawElements(GL_TRIANGLES, FBO.MESH.indices.length, GL_UNSIGNED_INT, 0);
        for (int i = 0; i < FBO.MATERIALS.length; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, FBO.ID);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Set clear color to opaque black
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearDepth(1.0f);  // Clearing depth to the farthest
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
    }

}
