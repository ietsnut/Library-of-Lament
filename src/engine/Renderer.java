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
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        aabbShader.render(scene);
        modelShader.render(scene);
        fboShader.unbind();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        fboShader.render(scene);
        glDisable(GL_BLEND);
    }

}
