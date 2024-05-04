package engine;

import content.Terrain;
import game.Game;
import game.Scene;
import object.*;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL40.*;

public class Renderer {

    private static final ModelShader   modelShader  = new ModelShader();
    private static final FBOShader     fboShader    = new FBOShader();
    private static final AABBShader    aabbShader   = new AABBShader();

    public static void render(Scene scene) {
        fboShader.bind();
        aabbShader.render(scene);
        modelShader.render(scene);
        fboShader.unbind();
        fboShader.render(scene);
    }

}
