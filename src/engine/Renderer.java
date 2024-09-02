package engine;

import game.Scene;

public class Renderer {

    private static EntityShader entityShader;
    private static FBOShader fboShader;
    private static AABBShader aabbShader;

    public static void init() {
        entityShader = new EntityShader();
        fboShader    = new FBOShader();
        aabbShader   = new AABBShader();
    }

    public static void render(Scene scene) {
        fboShader.bind();
        aabbShader.render(scene);
        entityShader.render(scene);
        fboShader.unbind();
        fboShader.render(scene);
    }

}
