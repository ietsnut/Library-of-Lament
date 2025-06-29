package shader;

import resource.Framebuffer;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class BloomShader extends Shader<Framebuffer[]> {

    private final static int PASSES = 16; // Number of ping-pong passes

    public BloomShader(Window window) {
        super(window, "bloom", "position");
    }

    @Override
    protected void shader(Framebuffer... framebuffers) {
        if (framebuffers.length < 3) {
            throw new IllegalArgumentException("BloomShader requires at least 3 framebuffers: [brightness, pingPong1, pingPong2]");
        }

        Framebuffer brightnessFramebuffer = framebuffers[0];
        Framebuffer pingPongFramebuffer1 = framebuffers[1];
        Framebuffer pingPongFramebuffer2 = framebuffers[2];

        boolean horizontal = true;
        boolean firstIteration = true;

        for (int i = 0; i < PASSES; i++) {
            Framebuffer targetFramebuffer = horizontal ? pingPongFramebuffer1 : pingPongFramebuffer2;

            targetFramebuffer.bind();

            uniform("horizontal", horizontal);
            uniform("width", targetFramebuffer.width);
            uniform("height", targetFramebuffer.height);

            glActiveTexture(GL_TEXTURE0);
            if (firstIteration) {
                glBindTexture(GL_TEXTURE_2D, brightnessFramebuffer.textures[0]);
            } else {
                Framebuffer sourceFramebuffer = horizontal ? pingPongFramebuffer2 : pingPongFramebuffer1;
                glBindTexture(GL_TEXTURE_2D, sourceFramebuffer.textures[0]);
            }

            window.quad.bind();
            glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
            window.quad.unbind();

            glBindTexture(GL_TEXTURE_2D, 0);
            targetFramebuffer.unbind();

            horizontal = !horizontal;
            if (firstIteration) firstIteration = false;
        }
    }
}