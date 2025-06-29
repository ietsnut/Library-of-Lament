package shader;

import resource.Framebuffer;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class BloomShader extends Shader<Framebuffer[]> {

    public BloomShader(Window window) {
        super(window, "bloom", "position");
    }

    @Override
    protected void shader(Framebuffer... framebuffers) {
        if (framebuffers.length < 3) {
            throw new IllegalArgumentException("BloomShader requires 3 framebuffers: [source, pingPong1, pingPong2]");
        }

        Framebuffer sourceFramebuffer = framebuffers[0]; // Main framebuffer with bright pixels in attachment 1
        Framebuffer pingPong1 = framebuffers[1];
        Framebuffer pingPong2 = framebuffers[2];

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        boolean horizontal = true;
        boolean firstIteration = true;
        int blurIterations = 10;

        for (int i = 0; i < blurIterations; i++) {
            Framebuffer targetFramebuffer = horizontal ? pingPong1 : pingPong2;
            targetFramebuffer.bind();

            uniform("horizontal", horizontal ? 1 : 0);

            if (firstIteration) {
                sourceFramebuffer.bindTexture(1, 0);
                firstIteration = false;
            } else {
                Framebuffer sourceBuffer = horizontal ? pingPong2 : pingPong1;
                sourceBuffer.bindTexture(0, 0);
            }

            window.quad.bind();
            glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
            window.quad.unbind();

            glBindTexture(GL_TEXTURE_2D, 0);
            targetFramebuffer.unbind();

            horizontal = !horizontal;
        }

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }
}