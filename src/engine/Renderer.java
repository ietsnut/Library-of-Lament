package engine;

import game.Scene;
import object.Entity;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;

public class Renderer {

    private static ModelShader   modelShader;
    private static FBOShader     fboShader;
    private static AABBShader    aabbShader;

    public static void init() {
        modelShader  = new ModelShader();
        fboShader    = new FBOShader();
        //aabbShader   = new AABBShader();
    }

    public static void render(Scene scene) {
        fboShader.bind();
        //aabbShader.render(scene);
        modelShader.render(scene);
        fboShader.unbind();
        fboShader.render(scene);
    }

}
