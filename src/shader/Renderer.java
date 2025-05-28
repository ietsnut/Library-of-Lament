package shader;

import engine.Scene;

public class Renderer {

    private static EntityShader entityShader;
    private static FBOShader    fboShader;
    private static AABBShader   aabbShader;
    private static GUIShader    guiShader;

    public static void init() {
        fboShader   = new FBOShader();
        entityShader= new EntityShader();
        aabbShader  = new AABBShader();
        guiShader   = new GUIShader();
    }

    public static void render(Scene scene) {
        fboShader.bind();
        aabbShader.render(scene);
        entityShader.render(scene);
        fboShader.unbind();
        fboShader.render(scene);
        //guiShader.render(scene);
    }

}
