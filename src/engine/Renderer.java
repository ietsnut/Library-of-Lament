package engine;

import game.Game;
import game.Scene;
import object.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import property.Load;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    public static final float NEAR      = 0.001f;
    public static final float FAR       = Terrain.SIZE * 4;

    public static final Matrix4f projection   = new Matrix4f();

    private final ModelShader   modelShader;
    private final SkyShader     skyShader;
    private final FBOShader     fboShader;
    private final AABBShader    aabbShader;

    public static final Camera camera = new Camera();

    public Renderer() {
        fboShader       = new FBOShader();
        modelShader     = new ModelShader();
        skyShader       = new SkyShader();
        aabbShader      = new AABBShader();
    }

    public void render(Scene scene) {
        projection();
        camera.update(scene);
        for (Entity entity : scene.entities) {
            entity.model();
        }
        fboShader.bind();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
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
