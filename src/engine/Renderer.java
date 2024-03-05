package engine;

import object.*;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public class Renderer {

    private static final float NEAR_PLANE = 0.01f;
    private static final float FAR_PLANE = 1000;

    private static final float RED = 0.5444f;
    private static final float GREEN = 0.62f;
    private static final float BLUE = 0.69f;

    private final ModelShader modelShader;
    private final TerrainShader terrainShader;
    private final SkyboxShader skyBoxShader;

    public Renderer() {
        //GL11.glEnable(GL11.GL_CULL_FACE);
        //GL11.glCullFace(GL11.GL_BACK);

        modelShader = new ModelShader();
        terrainShader = new TerrainShader();
        skyBoxShader = new SkyboxShader();
    }

    public void render(List<Light> lights, List<Model> models, Terrain terrain, Sky sky) {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(RED, GREEN, BLUE, 1);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        modelShader.start();
        modelShader.loadSkyColour(RED, GREEN, BLUE);
        modelShader.loadLights(lights);
        modelShader.loadViewMatrix();
        modelShader.render(models);
        modelShader.stop();
        terrainShader.start();
        terrainShader.loadSkyColour(RED, GREEN, BLUE);
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix();
        terrainShader.render(terrain);
        terrainShader.stop();
        skyBoxShader.render(sky);
    }

    public void clean() {
        for (Entity entity : Entity.ALL) {
            glDeleteVertexArrays(entity.vaoID);
            for (int vbo : entity.vboIDs) {
                glDeleteBuffers(vbo);
            }
        }
        for (Texture texture : Texture.ALL) {
            glDeleteTextures(texture.textureID);
        }
        modelShader.clean();
        terrainShader.clean();
        skyBoxShader.clean();
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
