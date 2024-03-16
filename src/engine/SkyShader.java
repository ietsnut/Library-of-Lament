package engine;

import object.Camera;
import object.Light;
import object.Sky;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class SkyShader extends Shader {

    public SkyShader() {
        super("sky");
    }

    public void render(Sky sky) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        start();
        for (int i = sky.layers.size() - 1; i >= 0; i--) {
            Sky.Layer layer = sky.layers.get(i);
            GL30.glBindVertexArray(layer.vaoID);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glEnable(GL_TEXTURE_2D);
            GL11.glBindTexture(GL_TEXTURE_2D, layer.texture.ID);
            loadModelMatrix(layer.getModelMatrix());
            loadViewMatrix(Camera.getViewMatrix());
            GL11.glDrawElements(GL11.GL_TRIANGLES, layer.indices.length, GL11.GL_UNSIGNED_INT, 0);
            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            GL30.glBindVertexArray(0);
        }
        stop();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "uv");
    }

    @Override
    protected void loadViewMatrix(Matrix4f matrix) {
        Matrix4f viewMatrix = new Matrix4f(matrix);
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        super.loadViewMatrix(viewMatrix);
    }
}
