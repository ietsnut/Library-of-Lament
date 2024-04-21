package engine;

import game.Game;
import game.Scene;
import object.FBO;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class FBOShader extends Shader {

    public FBO fbo;

    public FBOShader() {
        super("fbo", "position");
        this.fbo = new FBO(Display.getWidth(), Display.getHeight());
    }

    public void shader(Scene scene) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        uniform("width",            fbo.width);
        uniform("height",           fbo.height);
        uniform("colorTexture",     0);
        uniform("normalTexture",    1);
        render(fbo);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBuffer);
        GL11.glViewport(0, 0, fbo.width, fbo.height);
    }

    public void unbind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

}
