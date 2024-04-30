package engine;

import game.Game;
import game.Scene;
import object.FBO;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader {

    public FBO fbo;

    public FBOShader() {
        super("fbo", "position");
        this.fbo = new FBO(Game.WIDTH, Game.HEIGHT);
    }

    public void shader(Scene scene) {
        glDisable(GL_DEPTH_TEST);
        uniform("width",            fbo.width);
        uniform("height",           fbo.height);
        uniform("colorTexture",     0);
        uniform("normalTexture",    1);
        render(fbo);
        glEnable(GL_DEPTH_TEST);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.frameBuffer);
        glViewport(0, 0, fbo.width, fbo.height);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Game.WIDTH, Game.HEIGHT);
    }

}
