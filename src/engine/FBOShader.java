package engine;

import game.Game;
import game.Scene;
import object.Entity;
import object.FBO;

import static org.lwjgl.opengl.GL40.*;

public class FBOShader extends Shader {

    public FBO fbo;

    public FBOShader() {
        super("fbo", "position");
        this.fbo = new FBO();
    }

    public void shader(Scene scene) {
        glDisable(GL_DEPTH_TEST);
        render(fbo);
        glEnable(GL_DEPTH_TEST);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.frameBuffer);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

}
