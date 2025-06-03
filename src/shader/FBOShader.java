package shader;

import engine.Manager;
import engine.Scene;
import object.FBO;
import resource.Mesh;

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

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 1);
        glViewport(0, 0, Manager.windows[0].width, Manager.windows[0].height);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
    }

    public void shader(Scene scene) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glBindVertexArray(Mesh.QUAD.vao);
        glEnableVertexAttribArray(0);
        for (int i = 1; i < 4; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, i);
        }
        glDrawElements(GL_TRIANGLES, Mesh.QUAD.index, GL_UNSIGNED_INT, 0);
        for (int i = 1; i < 4; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Manager.windows[0].width, Manager.windows[0].height);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

}
