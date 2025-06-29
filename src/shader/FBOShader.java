package shader;

import resource.Framebuffer;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader<Framebuffer> {

    // Bloom properties
    private int bloomTexture = 0;

    public FBOShader(Window window) {
        super(window, "fbo", "position");
    }

    public void setBloomTexture(int texture) {
        this.bloomTexture = texture;
    }

    @Override
    protected void shader(Framebuffer framebuffer) {

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        uniform("width", framebuffer.width);
        uniform("height", framebuffer.height);

        window.quad.bind();

        // Bind FBO textures
        for (int i = 0; i < framebuffer.textures.length; i++) {
            uniform("texture" + (i + 1), i);
            framebuffer.bindTexture(i, i);
        }

        // Bind bloom texture if enabled
        if (bloomTexture != 0) {
            int bloomSlot = framebuffer.textures.length;
            uniform("bloomTexture", bloomSlot);
            glActiveTexture(GL_TEXTURE0 + bloomSlot);
            glBindTexture(GL_TEXTURE_2D, bloomTexture);
        }

        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);

        // Cleanup textures
        for (int i = 0; i < framebuffer.textures.length; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (bloomTexture != 0) {
            glActiveTexture(GL_TEXTURE0 + framebuffer.textures.length);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        window.quad.unbind();

    }
}