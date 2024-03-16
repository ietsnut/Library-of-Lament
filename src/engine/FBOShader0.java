package engine;

import object.Camera;
import object.FBO;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

public class FBOShader0 extends Shader {

    private int location_colorTexture;
    private int location_normalTexture;
    private int location_width;
    private int location_height;

    public FBO fbo;

    public FBOShader0(FBO fbo) {
        super("fbo0");
        this.fbo = fbo;
    }

    public void render() {
        this.start();

        GL30.glBindVertexArray(fbo.vaoID);

        super.loadInt(location_width, fbo.width);
        super.loadInt(location_height, fbo.height);
        super.loadInt(location_colorTexture, 0);
        super.loadInt(location_normalTexture, 1);

        GL20.glEnableVertexAttribArray(0);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fbo.colorTexture.ID);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, fbo.normalTexture.ID);

        glClear(GL_COLOR_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, 0);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        this.stop();
    }

    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBuffer);
        GL11.glViewport(0, 0, fbo.width, fbo.height);

    }

    public void unbind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    @Override
    protected void getAllUniformLocations() {
        location_colorTexture = super.getUniformLocation("colorTexture");
        location_normalTexture = super.getUniformLocation("normalTexture");
        location_width = super.getUniformLocation("width");
        location_height = super.getUniformLocation("height");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

}
