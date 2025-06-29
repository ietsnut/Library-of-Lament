package shader;

import resource.Framebuffer;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class BloomShader extends Shader<Framebuffer> {

    private int blurPasses = 16; // Number of ping-pong passes

    public BloomShader(Window window) {
        super(window, "bloom", "position");
    }

    public void renderBloom(Framebuffer brightnessFramebuffer, Framebuffer pingPongFramebuffer1, Framebuffer pingPongFramebuffer2) {
        // Enable blending for bloom effect
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);

        // Ping-pong blur passes
        boolean horizontal = true;
        boolean firstIteration = true;

        for (int i = 0; i < blurPasses; i++) {
            Framebuffer targetFramebuffer = horizontal ? pingPongFramebuffer1 : pingPongFramebuffer2;

            // Bind target framebuffer
            targetFramebuffer.bind();
            targetFramebuffer.clear(); // Clear the framebuffer

            start();
            uniform("horizontal", horizontal);
            uniform("width", targetFramebuffer.width);
            uniform("height", targetFramebuffer.height);

            // Bind source texture
            glActiveTexture(GL_TEXTURE0);
            if (firstIteration) {
                glBindTexture(GL_TEXTURE_2D, brightnessFramebuffer.textures[0]);
            } else {
                Framebuffer sourceFramebuffer = horizontal ? pingPongFramebuffer2 : pingPongFramebuffer1;
                glBindTexture(GL_TEXTURE_2D, sourceFramebuffer.textures[0]);
            }
            uniform("texture1", 0);

            // Render quad
            glBindVertexArray(window.quad.vao);
            glEnableVertexAttribArray(0);
            glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
            glDisableVertexAttribArray(0);
            glBindVertexArray(0);

            stop();

            targetFramebuffer.unbind();

            horizontal = !horizontal;
            if (firstIteration) firstIteration = false;
        }

        // Clean up
        glBindTexture(GL_TEXTURE_2D, 0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    @Override
    protected void shader(Framebuffer framebuffer) {
        // This method is required by the parent class but not used in our bloom implementation
        // The actual rendering is handled by renderBloom method above
    }
}