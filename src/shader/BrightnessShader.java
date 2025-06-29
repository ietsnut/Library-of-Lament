package shader;

import resource.Framebuffer;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class BrightnessShader extends Shader<Framebuffer> {

    private float brightnessThreshold = 0.8f;

    public BrightnessShader(Window window) {
        super(window, "brightness", "position");
    }

    public void setBrightnessThreshold(float threshold) {
        this.brightnessThreshold = threshold;
    }

    public void extractBrightness(Framebuffer sourceFramebuffer, Framebuffer brightnessFramebuffer) {
        // Bind brightness extraction framebuffer
        brightnessFramebuffer.bind();
        brightnessFramebuffer.clear(); // Clear the framebuffer

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);

        start();
        uniform("brightnessThreshold", brightnessThreshold);
        uniform("width", sourceFramebuffer.width);
        uniform("height", sourceFramebuffer.height);

        sourceFramebuffer.bindTexture(0, 0);

        uniform("texture1", 0);

        // Render quad
        glBindVertexArray(window.quad.vao);
        glEnableVertexAttribArray(0);
        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        stop();

        brightnessFramebuffer.unbind();
        glBindTexture(GL_TEXTURE_2D, 0);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    @Override
    protected void shader(Framebuffer framebuffer) {
        // This method is required by the parent class but not used in our brightness extraction
        // The actual rendering is handled by extractBrightness method above
    }
}