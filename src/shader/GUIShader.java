package shader;

import engine.Console;
import engine.Manager;
import engine.Scene;
import property.GUI;
import resource.Mesh;
import window.Main;
import window.Map;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class GUIShader extends Shader<GUI> {

    public GUIShader(Window window) {
        super(window, "GUI", "position");
        start();
        uniform("texture1", 0);
        stop();
    }

    @Override
    protected void shader(GUI gui) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBindVertexArray(window.quad.vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gui.material.id);
        uniform("texture1", 0);
        uniform("guiPosition", gui.position);
        uniform("guiScale", gui.scale);
        uniform("guiRotation", gui.rotation);
        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

    }

}
