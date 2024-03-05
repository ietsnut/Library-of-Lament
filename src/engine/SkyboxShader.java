package engine;

import object.Camera;
import object.Model;
import object.Sky;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;

public class SkyboxShader extends Shader {

    public SkyboxShader() {
        super("skybox");
    }

    public void render(Sky sky) {
        start();
        loadViewMatrix();
        GL30.glBindVertexArray(sky.vaoID);
        GL20.glEnableVertexAttribArray(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, sky.texID);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, sky.vertices.length / 3);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        stop();
    }

    @Override
    public void loadViewMatrix() {
        Matrix4f matrix = Camera.getViewMatrix();
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        super.loadViewMatrix(matrix);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }


}
