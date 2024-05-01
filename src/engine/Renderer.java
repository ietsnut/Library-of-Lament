package engine;

import content.Terrain;
import game.Game;
import game.Scene;
import object.*;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL40.*;

public class Renderer {

    public static final float NEAR      = 0.001f;
    public static final float FAR       = Byte.MAX_VALUE * 4;

    public static final Matrix4f projection   = new Matrix4f();

    private final ModelShader   modelShader;
    private final SkyShader     skyShader;
    private final FBOShader     fboShader;
    private final AABBShader    aabbShader;

    public Renderer() {
        fboShader       = new FBOShader();
        modelShader     = new ModelShader();
        skyShader       = new SkyShader();
        aabbShader      = new AABBShader();
    }

    public void render(Scene scene) {
        Camera.move();
        Camera.view();
        projection();
        for (Entity entity : scene.entities) {
            entity.model();
        }
        fboShader.bind();
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        aabbShader.render(scene);
        modelShader.render(scene);

        //skyShader.render(scene);
        fboShader.unbind();
        fboShader.render(scene);

    }

    public static void projection() {
        projection.identity().perspective((float) Math.toRadians(Camera.FOV), (float) Game.WIDTH / (float) Game.HEIGHT, NEAR, FAR);
    }

}
