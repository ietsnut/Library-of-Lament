package shader;

import resource.Framebuffer;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class BrightnessShader extends Shader<Framebuffer[]> {

    public BrightnessShader(Window window) {
        super(window, "brightness", "position");
    }

    @Override
    protected void shader(Framebuffer... framebuffers) {
        if (framebuffers.length < 2) {
            throw new IllegalArgumentException("BrightnessShader requires at least 2 framebuffers: [source, brightness]");
        }

        Framebuffer sourceFramebuffer = framebuffers[0];
        Framebuffer brightnessFramebuffer = framebuffers[1];

        brightnessFramebuffer.bind();

        uniform("width", sourceFramebuffer.width);
        uniform("height", sourceFramebuffer.height);

        sourceFramebuffer.bindTexture(0, 0);

        glBindVertexArray(window.quad.vao);
        glEnableVertexAttribArray(0);
        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_2D, 0);
        brightnessFramebuffer.unbind();
    }
}