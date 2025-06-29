package shader;

import org.lwjgl.BufferUtils;
import resource.Framebuffer;
import window.Window;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader<Framebuffer> {

    private final IntBuffer oldViewport = BufferUtils.createIntBuffer(4);

    // Bloom properties
    private int bloomTexture = 0;
    private boolean enableBloom = false;
    private float bloomIntensity = 0.5f;

    public FBOShader(Window window) {
        super(window, "fbo", "position");
    }

    public void bind(Framebuffer framebuffer) {
        framebuffer.bind();
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // glEnable(GL_MULTISAMPLE);
        glDepthRange(0.0, 1.0);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
    }

    public void setBloomTexture(int texture) {
        this.bloomTexture = texture;
    }

    public void setBloomSettings(boolean enabled, float intensity) {
        this.enableBloom = enabled;
        this.bloomIntensity = intensity;
    }

    @Override
    protected void shader(Framebuffer framebuffer) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        uniform("width", framebuffer.width);
        uniform("height", framebuffer.height);
        uniform("enableBloom", enableBloom);
        uniform("bloomIntensity", bloomIntensity);

        glBindVertexArray(window.quad.vao);
        glEnableVertexAttribArray(0);


        // Bind FBO textures
        for (int i = 0; i < framebuffer.textures.length; i++) {
            uniform("texture" + (i + 1), i);
            framebuffer.bindTexture(i, i);
        }

        // Bind bloom texture if enabled
        if (enableBloom && bloomTexture != 0) {
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
        if (enableBloom && bloomTexture != 0) {
            glActiveTexture(GL_TEXTURE0 + framebuffer.textures.length);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }
}