package shader;

import resource.FBO;
import resource.BloomFBO;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class BrightnessShader extends Shader<FBO> {

    private float brightnessThreshold = 0.8f;

    public BrightnessShader(Window window) {
        super(window, "brightness", "position");
    }

    public void setBrightnessThreshold(float threshold) {
        this.brightnessThreshold = threshold;
    }

    public void extractBrightness(FBO sourceFBO, BloomFBO brightnessFBO) {
        // Store current viewport
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);

        // Bind brightness extraction framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, brightnessFBO.framebuffer);
        glViewport(0, 0, brightnessFBO.width, brightnessFBO.height);
        glClear(GL_COLOR_BUFFER_BIT);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);

        start();
        uniform("brightnessThreshold", brightnessThreshold);
        uniform("width", sourceFBO.width);
        uniform("height", sourceFBO.height);

        // Bind source texture (the rendered scene)
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceFBO.textures[0]);
        uniform("texture1", 0);

        // Render quad
        glBindVertexArray(window.quad.vao);
        glEnableVertexAttribArray(0);
        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        stop();

        // Restore viewport and unbind
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        glBindTexture(GL_TEXTURE_2D, 0);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    @Override
    protected void shader(FBO fbo) {
        // This method is required by the parent class but not used in our brightness extraction
        // The actual rendering is handled by extractBrightness method above
    }
}