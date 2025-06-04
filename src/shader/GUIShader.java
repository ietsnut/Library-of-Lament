package shader;

import engine.Console;
import engine.Manager;
import engine.Scene;
import property.GUI;
import resource.Mesh;
import window.Main;
import window.Map;

import static org.lwjgl.opengl.GL40.*;

public class GUIShader extends Shader<GUI> {

    Mesh QUAD;

    public GUIShader() {
        super("GUI", "position");
        start();
        uniform("texture1", 0);
        stop();
        QUAD = new Mesh() {
            @Override
            public void load() {
                vertices = new byte[] {-1, 1, -1, -1, 1, 1, 1, -1};
                indices  = new int[] {0, 1, 2, 2, 1, 3};
                uvs = new float[]{ 0, 0, 1, 0, 1, 1, 0, 1 };
            }
            @Override
            public int dimensions() {
                return 2;
            }
        };
    }


    @Override
    protected void shader(GUI gui) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindVertexArray(QUAD.vao);
        glEnableVertexAttribArray(0);

        uniform("guiPosition", gui.position);
        uniform("guiScale", gui.scale);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gui.material.id);

        glDrawElements(GL_TRIANGLES, QUAD.index, GL_UNSIGNED_INT, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

}
