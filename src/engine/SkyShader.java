package engine;

import object.Camera;
import object.Sky;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix4f;

public class SkyShader extends Shader {

    public SkyShader() {
        super("sky");
    }

    public void render(Sky sky) {
        start();
        loadModelMatrix(sky.getModelMatrix());
        loadViewMatrix(Camera.getViewMatrix());
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
    public void loadViewMatrix(Matrix4f matrix) {
        Matrix4f viewMatrix = new Matrix4f(matrix);
        viewMatrix.m30 = 0;
        viewMatrix.m31 = 0;
        viewMatrix.m32 = 0;
        super.loadViewMatrix(viewMatrix);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

}
