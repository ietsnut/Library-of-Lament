package engine;

import game.Game;
import game.Scene;

public class Renderer {

    private static ModelShader   modelShader;
    private static FBOShader     fboShader;
    private static AABBShader    aabbShader;

    public static void init() {
        modelShader  = new ModelShader();
        fboShader    = new FBOShader();
        aabbShader   = new AABBShader();
    }

    public static void render() {
        fboShader.bind();
        aabbShader.render();
        modelShader.render();
        fboShader.unbind();
        fboShader.render();
    }

}
