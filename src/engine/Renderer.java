package engine;

import game.Game;
import game.Scene;
import object.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    public static final float NEAR_PLANE = 0.001f;
    public static final float FAR_PLANE = Terrain.SIZE * 4;

    private final ModelShader   modelShader;
    private final TerrainShader terrainShader;
    private final SkyShader     skyShader;
    private final FBOShader     fboShader;
    private final AABBShader    aabbShader;

    public static final Camera camera = new Camera();

    public Renderer() {
        modelShader     = new ModelShader();
        terrainShader   = new TerrainShader();
        skyShader       = new SkyShader();
        fboShader       = new FBOShader();
        aabbShader      = new AABBShader();
    }

    public void render(Scene scene) {
        camera.update(scene);
        fboShader.bind();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        aabbShader.render(scene);
        modelShader.render(scene);
        terrainShader.render(scene);
        skyShader.render(scene);
        fboShader.unbind();
        fboShader.render(scene);
    }

    public void clean() {
        for (Entity entity : Entity.ALL) {
            glDeleteVertexArrays(entity.vaoID);
            for (int vbo : entity.vboIDs) {
                glDeleteBuffers(vbo);
            }
            if (entity instanceof FBO fbo) {
                glDeleteFramebuffers(fbo.frameBuffer);
                glDeleteFramebuffers(fbo.drawBuffers);
                glDeleteRenderbuffers(fbo.depthBuffer);
            }
        }
        for (Texture texture : Texture.ALL) {
            glDeleteTextures(texture.ID);
        }
        Shader.clean();
    }

    public static Matrix4f projection() {
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(camera.FOV / 2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
        return projectionMatrix;
    }
}
