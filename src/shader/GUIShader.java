package shader;

import property.GUI;
import window.Window;

import static org.lwjgl.opengl.GL40.*;

public class GUIShader extends Shader<GUI> {

    public GUIShader(Window window) {
        super(window, "GUI", "position");
    }

    @Override
    protected void shader(GUI gui) {

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        window.quad.bind();
        gui.material.bind();

        uniform("guiPosition", gui.position);
        uniform("guiScale", gui.scale);
        uniform("guiRotation", gui.rotation);

        glDrawElements(GL_TRIANGLES, window.quad.index, GL_UNSIGNED_INT, 0);

        gui.material.unbind();
        window.quad.unbind();

    }
}