package engine;

import game.Scene;
import object.*;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import property.Load;

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
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        aabbShader.render(scene);
        modelShader.render(scene);
        //skyShader.render(scene);
        fboShader.unbind();
        fboShader.render(scene);
    }

    public void clean() {
        for (Load load : Load.BOUND) {
            if (load instanceof Texture texture) {
                glDeleteTextures(texture.id);
            }
            if (load instanceof Entity entity) {
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
        }
        Shader.clean();
    }

    public static void projection() {
        projection.setIdentity();
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(Camera.FOV / 2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR - NEAR;
        projection.m00 = x_scale;
        projection.m11 = y_scale;
        projection.m22 = -((FAR + NEAR) / frustum_length);
        projection.m23 = -1;
        projection.m32 = -((2 * NEAR * FAR) / frustum_length);
        projection.m33 = 0;
    }

}
