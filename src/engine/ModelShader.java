package engine;

import object.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

public class ModelShader extends Shader {

    private int[] location_lightPosition;
    private int[] location_lightAttenuation;
    private int[] location_lightIntensity;
    private int location_near;
    private int location_far;

    public ModelShader() {
        super("model");
    }

    public void render(List<Entity> models) {
        super.loadFloat(location_near, Renderer.NEAR_PLANE);
        super.loadFloat(location_far, Renderer.FAR_PLANE);
        for (Entity model : models) {
            GL30.glBindVertexArray(model.vaoID);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glEnable(GL_TEXTURE_2D);
            GL11.glBindTexture(GL_TEXTURE_2D, model.texture.ID);
            loadModelMatrix(model.getModelMatrix());
            GL11.glDrawElements(GL11.GL_TRIANGLES, model.indices.length, GL11.GL_UNSIGNED_INT, 0);
            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            GL30.glBindVertexArray(0);
        }
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "uv");
        super.bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        super.getAllUniformLocations();
        location_lightPosition = new int[Light.ALL.size()];
        location_lightAttenuation = new int[Light.ALL.size()];
        location_lightIntensity = new int[Light.ALL.size()];
        for (int i = 0; i < Light.ALL.size(); i++) {
            location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
            location_lightAttenuation[i] = super.getUniformLocation("lightAttenuation[" + i + "]");
            location_lightIntensity[i] = super.getUniformLocation("lightIntensity[" + i + "]");
        }
        location_near = super.getUniformLocation("near");
        location_far = super.getUniformLocation("far");
    }

    //TODO: REPLACE WITH SCENE SPECIFIC ARRAYS
    public void loadLights(List<Light> lights) {
        for (int i = 0; i < Light.ALL.size(); i++) {
            super.loadVector(location_lightPosition[i], lights.get(i).position);
            super.loadVector(location_lightAttenuation[i], lights.get(i).attenuation);
            super.loadFloat(location_lightIntensity[i], lights.get(i).intensity);
        }
    }
}
