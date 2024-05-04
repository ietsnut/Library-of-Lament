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
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        render(fbo);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.frameBuffer);
        glViewport(0, 0, Game.WIDTH, Game.HEIGHT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Set clear color to opaque black
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearDepth(1.0f);  // Clearing depth to the farthest
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Game.WIDTH, Game.HEIGHT);
    }

}
