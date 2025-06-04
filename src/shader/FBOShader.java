package shader;

import engine.Console;
import engine.Manager;
import org.lwjgl.BufferUtils;
import resource.FBO;
import resource.Mesh;
import window.Main;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL44.glClearTexImage;

public class FBOShader extends Shader<FBO> {

    public FBOShader() {
        super("fbo", "position");
        start();
        uniform("texture1", 1);
        uniform("texture2", 2);
        uniform("texture3", 3);
        stop();
    }

    IntBuffer clearValue = BufferUtils.createIntBuffer(1).put(128).flip();

    public void bind(FBO fbo) {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.framebuffer);
        glViewport(0, 0, Manager.main.width, Manager.main.height);
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDepthRange(0.0, 1.0);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
    }

    public void unbind(FBO fbo) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Manager.main.width, Manager.main.height);
    }

    @Override
    protected void shader(FBO fbo) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);

        glBindVertexArray(Mesh.QUAD.vao);
        glEnableVertexAttribArray(0);
        for (int i = 0; i < fbo.textures.length; i++) {
            glActiveTexture(GL_TEXTURE0 + (i + 1));
            glBindTexture(GL_TEXTURE_2D, fbo.textures[i]);
        }

        glDrawElements(GL_TRIANGLES, Mesh.QUAD.index, GL_UNSIGNED_INT, 0);
        for (int i = 0; i < fbo.textures.length; i++) {
            glActiveTexture(GL_TEXTURE0 + (i + 1));
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

}
