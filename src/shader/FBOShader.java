package shader;

import resource.Framebuffer;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader<Framebuffer[]> {

    public FBOShader(Window window) {
        super(window, "fbo", "position");
    }

    @Override
    protected void shader(Framebuffer... framebuffers) {

        Framebuffer framebuffer = framebuffers[0];
        int bloomTexture = framebuffers[1].textures[0];

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        uniform("width", framebuffer.width);
        uniform("height", framebuffer.height);

        window.quad.bind();

        for (int i = 0; i < framebuffer.textures.length; i++) {
            framebuffer.bindTexture(i, i);
        }

        if (bloomTexture != 0) {
            int bloomSlot = framebuffer.textures.length;
            uniform("texture4", bloomSlot);
            glActiveTexture(GL_TEXTURE0 + bloomSlot);
            glBindTexture(GL_TEXTURE_2D, bloomTexture);
        }

        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);

        for (int i = 0; i < framebuffer.textures.length; i++) {
            framebuffer.unbindTexture(i);
        }
        if (bloomTexture != 0) {
            glActiveTexture(GL_TEXTURE0 + framebuffer.textures.length);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        window.quad.unbind();

    }
}