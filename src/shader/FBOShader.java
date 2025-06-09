package shader;

import engine.Manager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40;
import resource.FBO;
import resource.Mesh;
import window.Window;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader<FBO> {

    public FBOShader(Window window) {
        super(window, "fbo", "position");
    }

    private final IntBuffer oldViewport = BufferUtils.createIntBuffer(4);

    public void bind(FBO fbo) {
        glGetIntegerv(GL_VIEWPORT, oldViewport);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.framebuffer);
        glViewport(0, 0, fbo.width, fbo.height);
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //glEnable(GL_MULTISAMPLE);
        glDepthRange(0.0, 1.0);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
    }

    public void unbind(FBO fbo) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, oldViewport.get(2), oldViewport.get(3));
        glEnable(GL_MULTISAMPLE);
    }

    @Override
    protected void shader(FBO fbo) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        uniform("width", fbo.width);
        uniform("height", fbo.height);
        glBindVertexArray(window.quad.vao);
        glEnableVertexAttribArray(0);
        for (int i = 0; i < fbo.textures.length; i++) {
            uniform("texture" + (i + 1), i + 1);
            glActiveTexture(GL_TEXTURE0 + (i + 1));
            glBindTexture(GL_TEXTURE_2D, fbo.textures[i]);
        }
        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
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
