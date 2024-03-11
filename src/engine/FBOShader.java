package engine;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

public class FBOShader extends Shader {

    public FBOShader() {
        super("fbo");
    }

    public void render() {
        this.start();

        GL30.glBindVertexArray(Renderer.fbo.vaoID);
        GL20.glEnableVertexAttribArray(0);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, Renderer.fbo.texture.ID);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Display.getWidth(), Display.getHeight());

        glClear(GL_COLOR_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        this.stop();
    }

    @Override
    protected void getAllUniformLocations() {}

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

}
