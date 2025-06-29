package shader;

import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class EffectShader extends Shader<Void> {

    public EffectShader(Window window, String shaderName) {
        super(window, shaderName, "position");
    }

    @Override
    protected void shader(Void unused) {
        glBindVertexArray(window.quad.vao);
        glEnableVertexAttribArray(0);
        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }

}