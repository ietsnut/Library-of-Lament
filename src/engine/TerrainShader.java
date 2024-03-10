package engine;

import object.Light;
import object.Model;
import object.Terrain;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.*;

public class TerrainShader extends Shader {

    private int[] location_lightPosition;
    private int[] location_lightAttenuation;
    private int location_backgroundTexture;
    private int location_rTexture;
    private int location_gTexture;
    private int location_bTexture;
    private int location_blendMap;

    public TerrainShader() {
        super("terrain");
        start();
        super.loadInt(location_backgroundTexture, 0);
        super.loadInt(location_rTexture, 1);
        super.loadInt(location_gTexture, 2);
        super.loadInt(location_bTexture, 3);
        super.loadInt(location_blendMap, 4);
        stop();
    }

    public void render(Terrain terrain) {
        GL30.glBindVertexArray(terrain.vaoID);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_TEXTURE_3D);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, terrain.texture.textureID);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, terrain.textureR.textureID);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, terrain.textureG.textureID);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, terrain.textureB.textureID);
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, terrain.blendMap.textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        loadModelMatrix(terrain.getModelMatrix());
        GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.indices.length, GL11.GL_UNSIGNED_INT, 0);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
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
        for (int i = 0; i < Light.ALL.size(); i++) {
            location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
        }
        for (int i = 0; i < Light.ALL.size(); i++) {
            location_lightAttenuation[i] = super.getUniformLocation("lightAttenuation[" + i + "]");
        }
        location_backgroundTexture = super.getUniformLocation("backgroundTexture");
        location_rTexture = super.getUniformLocation("rTexture");
        location_gTexture = super.getUniformLocation("gTexture");
        location_bTexture = super.getUniformLocation("bTexture");
        location_blendMap = super.getUniformLocation("blendMap");
    }

    //TODO: REPLACE WITH SCENE SPECIFIC ARRAYS
    public void loadLights(List<Light> lights) {
        for (int i = 0; i < Light.ALL.size(); i++) {
            super.loadVector(location_lightPosition[i], lights.get(i).position);
            super.loadVector(location_lightAttenuation[i], lights.get(i).attenuation);
        }
    }
}
