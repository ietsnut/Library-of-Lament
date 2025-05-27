package shader;

import engine.Scene;
import property.GUI;
import resource.Mesh;

import static org.lwjgl.opengl.GL40.*;

public class GUIShader extends Shader {

    public GUIShader() {
        super("GUI", "position", "uv");
        start();
        uniform("texture1", 0);
        stop();
    }

    @Override
    protected void shader(Scene scene) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindVertexArray(Mesh.QUAD.vao);
        glEnableVertexAttribArray(0);
        for (GUI gui : scene.guis) {
            render(gui);
        }
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void render(GUI gui) {
        uniform("guiPosition", gui.position);
        uniform("guiScale", gui.scale);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gui.material.texture);

        glDrawElements(GL_TRIANGLES, Mesh.QUAD.index, GL_UNSIGNED_INT, 0);

        glBindTexture(GL_TEXTURE_2D, 0);
    }


}
