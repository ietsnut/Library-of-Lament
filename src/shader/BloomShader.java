package shader;

import resource.FBO;
import resource.BloomFBO;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class BloomShader extends Shader<FBO> {

    private boolean horizontal = true;
    private int blurPasses = 10; // Number of ping-pong passes

    public BloomShader(Window window) {
        super(window, "bloom", "position");
    }

    public void setBlurPasses(int passes) {
        this.blurPasses = passes;
    }

    public void renderBloom(BloomFBO brightnessFBO, BloomFBO pingPongFBO1, BloomFBO pingPongFBO2) {
        // Store current viewport
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);

        // Enable blending for bloom effect
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);

        // Ping-pong blur passes
        boolean horizontal = true;
        boolean firstIteration = true;

        for (int i = 0; i < blurPasses; i++) {
            BloomFBO targetFBO = horizontal ? pingPongFBO1 : pingPongFBO2;

            // Bind target framebuffer
            glBindFramebuffer(GL_FRAMEBUFFER, targetFBO.framebuffer);
            glViewport(0, 0, targetFBO.width, targetFBO.height);
            glClear(GL_COLOR_BUFFER_BIT);

            start();
            uniform("horizontal", horizontal);
            uniform("width", targetFBO.width);
            uniform("height", targetFBO.height);

            // Bind source texture
            glActiveTexture(GL_TEXTURE0);
            if (firstIteration) {
                glBindTexture(GL_TEXTURE_2D, brightnessFBO.colorTexture);
            } else {
                BloomFBO sourceFBO = horizontal ? pingPongFBO2 : pingPongFBO1;
                glBindTexture(GL_TEXTURE_2D, sourceFBO.colorTexture);
            }
            uniform("texture1", 0);

            // Render quad
            glBindVertexArray(window.quad.vao);
            glEnableVertexAttribArray(0);
            glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
            glDisableVertexAttribArray(0);
            glBindVertexArray(0);

            stop();

            horizontal = !horizontal;
            if (firstIteration) firstIteration = false;
        }

        // Restore viewport and unbind framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        glBindTexture(GL_TEXTURE_2D, 0);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    @Override
    protected void shader(FBO fbo) {
        // This method is required by the parent class but not used in our bloom implementation
        // The actual rendering is handled by renderBloom method above
    }
}