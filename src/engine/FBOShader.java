package engine;

import game.Game;
import game.Scene;
import object.FBO;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import tool.Random;

public class FBOShader extends Shader {

    public FBO fbo;

    public FBOShader() {
        super("fbo", "position");
        this.fbo = new FBO(Display.getWidth(), Display.getHeight());;
    }

    public void shader(Scene scene) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        //float t = (Sys.getTime() * 1000.0f) / Sys.getTimerResolution();
        //uniform("time",         t / 1000.0f);
        uniform("width",            fbo.width);
        uniform("height",           fbo.height);
        /*
        float normalizedJFramePosX = (float) (Game.stone.getLocationOnScreen().getX() - Display.getX()) / Display.getWidth();
        float normalizedJFramePosY = 1.0f - ((float)(Game.stone.getLocationOnScreen().getY() - Display.getY()) / Display.getHeight()) - (float) Game.stone.getHeight() / Display.getHeight();
        float normalizedJFrameWidth = (float) Game.stone.getWidth() / Display.getWidth();
        float normalizedJFrameHeight = (float) Game.stone.getHeight() / Display.getHeight();
        uniform("stoneX",           normalizedJFramePosX);
        uniform("stoneY",           normalizedJFramePosY);
        uniform("stoneW",           normalizedJFrameWidth);
        uniform("stoneH",           normalizedJFrameHeight);
        */
        uniform("colorTexture",     0);
        uniform("normalTexture",    1);
        uniform("noiseTexture",     2);
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
