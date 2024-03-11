package engine;

import object.*;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private static final float NEAR_PLANE = 0.01f;
    private static final float FAR_PLANE = 2000;

    private final ModelShader modelShader;
    private final TerrainShader terrainShader;
    private final SkyShader skyShader;
    private final FBOShader fboShader;

    public static float delta;
    private static float lastTime;

    public static final FBO fbo = new FBO(Display.getWidth(), Display.getHeight());

    public Renderer() {
        //GL11.glEnable(GL11.GL_CULL_FACE);
        //GL11.glCullFace(GL11.GL_BACK);
        modelShader = new ModelShader();
        terrainShader = new TerrainShader();
        skyShader = new SkyShader();
        fboShader = new FBOShader();
    }

    public void render(List<Light> lights, List<Entity> models, Terrain terrain, Sky sky) {
        float currentFrameTime = (Sys.getTime() * 1000f) / Sys.getTimerResolution();
        delta = currentFrameTime - lastTime;
        lastTime = currentFrameTime;
        Camera.move(terrain);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBuffer);
        GL11.glViewport(0, 0, fbo.width, fbo.height);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        skyShader.render(sky);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        modelShader.start();
        modelShader.loadLights(lights);
        modelShader.loadViewMatrix(Camera.getViewMatrix());
        modelShader.render(models);
        modelShader.stop();
        terrainShader.start();
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix(Camera.getViewMatrix());
        terrainShader.render(terrain);
        terrainShader.stop();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
        fboShader.render();
    }

    public void clean() {
        for (List<Entity> entities : Entity.ALL.values()) {
            for (Entity entity : entities) {
                glDeleteVertexArrays(entity.vaoID);
                for (int vbo : entity.vboIDs) {
                    glDeleteBuffers(vbo);
                }
                if (entity instanceof FBO fbo) {
                    glDeleteFramebuffers(fbo.frameBuffer);
                    glDeleteRenderbuffers(fbo.depthBuffer);
                }
            }
        }
        for (Texture texture : Texture.ALL) {
            glDeleteTextures(texture.ID);
        }
        Shader.clean();
    }

    public static Matrix4f getProjectionMatrix() {
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(Camera.FOV / 2f))) * aspectRatio);
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
