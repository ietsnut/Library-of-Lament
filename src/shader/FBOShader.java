package shader;

import engine.Console;
import engine.Manager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40;
import resource.FBO;
import resource.Mesh;
import window.Window;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader<FBO> {

    private final IntBuffer oldViewport = BufferUtils.createIntBuffer(4);

    // Bloom properties
    private int bloomTexture = 0;
    private boolean enableBloom = false;
    private float bloomIntensity = 0.5f;

    public FBOShader(Window window) {
        super(window, "fbo", "position");
    }

    public void bind(FBO fbo) {
        glGetIntegerv(GL_VIEWPORT, oldViewport);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.framebuffer);
        glViewport(0, 0, fbo.width, fbo.height);
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

    public void unbind(FBO fbo) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, oldViewport.get(2), oldViewport.get(3));
        // glEnable(GL_MULTISAMPLE);
    }

    public void setBloomTexture(int texture) {
        this.bloomTexture = texture;
    }

    public void setBloomSettings(boolean enabled, float intensity) {
        this.enableBloom = enabled;
        this.bloomIntensity = intensity;
    }

    @Override
    protected void shader(FBO fbo) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        uniform("width", fbo.width);
        uniform("height", fbo.height);
        uniform("enableBloom", enableBloom);
        uniform("bloomIntensity", bloomIntensity);

        glBindVertexArray(window.quad.vao);
        glEnableVertexAttribArray(0);

        // Bind FBO textures
        for (int i = 0; i < fbo.textures.length; i++) {
            uniform("texture" + (i + 1), i);
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, fbo.textures[i]);
        }

        // Bind bloom texture if enabled
        if (enableBloom && bloomTexture != 0) {
            int bloomSlot = fbo.textures.length;
            uniform("bloomTexture", bloomSlot);
            glActiveTexture(GL_TEXTURE0 + bloomSlot);
            glBindTexture(GL_TEXTURE_2D, bloomTexture);
        }

        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);

        // Cleanup textures
        for (int i = 0; i < fbo.textures.length; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (enableBloom && bloomTexture != 0) {
            glActiveTexture(GL_TEXTURE0 + fbo.textures.length);
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }
}