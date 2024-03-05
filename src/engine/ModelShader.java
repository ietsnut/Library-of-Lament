package engine;

import object.Camera;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import object.Light;
import object.Model;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

public class ModelShader extends Shader {

    private int location_transformationMatrix;
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_skyColour;
    private int[] location_lightPosition;
    private int[] location_lightIntensity;

    public ModelShader() {
        super("model");
    }

    public void render(List<Model> models) {
        for (Model model : models) {
            GL30.glBindVertexArray(model.vaoID);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);
            super.loadFloat(location_shineDamper, location_shineDamper);
            super.loadFloat(location_reflectivity, location_reflectivity);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glEnable(GL_TEXTURE_2D);
            GL11.glBindTexture(GL_TEXTURE_2D, model.texture.textureID);
            super.loadMatrix(location_transformationMatrix, model.getTransformationMatrix());
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
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        super.getAllUniformLocations();
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_skyColour = super.getUniformLocation("skyColour");
        location_lightPosition = new int[Light.ALL.size()];
        location_lightIntensity = new int[Light.ALL.size()];
        for (int i = 0; i < Light.ALL.size(); i++) {
            location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
        }
        for (int i = 0; i < Light.ALL.size(); i++) {
            location_lightIntensity[i] = super.getUniformLocation("lightIntensity[" + i + "]");
        }
    }

    public void loadSkyColour(float r, float g, float b) {
        super.loadVector(location_skyColour, new Vector3f(r, g, b));
    }

    public void loadLights(List<Light> lights) {
        for (int i = 0; i < Light.ALL.size(); i++) {
            if (i < lights.size()) {
                super.loadVector(location_lightPosition[i], lights.get(i).position);
                super.loadVector(location_lightIntensity[i], lights.get(i).intensity);
            } else {
                super.loadVector(location_lightPosition[i], new Vector3f(0, 0, 0));
                super.loadVector(location_lightIntensity[i], new Vector3f(1, 0, 0));
            }
        }
    }

}
