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

    public GUIShader() {
        super("GUI", "position");
        start();
        uniform("texture1", 0);
        stop();
    }


    @Override
    protected void shader(GUI gui) {
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            Console.error("OpenGL Error before shader start: " + error);
        }

        // Check if shader program is valid
        if (glGetInteger(GL_CURRENT_PROGRAM) == 0) {
            Console.error("No shader program bound!");
            return;
        }

        // Check if resources are valid
        if (Manager.map.quad.vao == 0) {
            Console.error("QUAD VAO not bound!");
            return;
        }

        if (gui.material == null || gui.material.id == 0) {
            Console.error("Material not bound!");
            return;
        }

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindVertexArray(Manager.map.quad.vao);

        // Check for errors after VAO binding
        error = glGetError();
        if (error != GL_NO_ERROR) {
            Console.error("OpenGL Error after VAO binding: " + error);
        }

        // Enable vertex attributes
        glEnableVertexAttribArray(0);
        if (Manager.map.quad.uvs != null) {
            glEnableVertexAttribArray(1);
        }

        // Bind texture first
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gui.material.id);

        error = glGetError();
        if (error != GL_NO_ERROR) {
            Console.error("OpenGL Error after texture binding: " + error);
        }

        // Set uniforms after texture binding
        uniform("texture1", 0);
        uniform("guiPosition", gui.position);
        uniform("guiScale", gui.scale);

        // Check for errors after uniform setting
        error = glGetError();
        if (error != GL_NO_ERROR) {
            Console.error("OpenGL Error after setting uniforms: " + error);
        }

        // Draw
        glDrawElements(GL_TRIANGLES, Manager.map.quad.index, GL_UNSIGNED_INT, 0);

        // Check for errors after drawing
        error = glGetError();
        if (error != GL_NO_ERROR) {
            Console.error("OpenGL Error after drawing: " + error);
        }

        // Cleanup
        glBindTexture(GL_TEXTURE_2D, 0);
        glDisableVertexAttribArray(0);
        if (Manager.map.quad.uvs != null) {
            glDisableVertexAttribArray(1);
        }
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

    }

}
