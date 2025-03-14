package shader;

import object.*;
import object.Automata;

import static engine.Renderer.automata;
import static org.lwjgl.opengl.GL40.*;

public class AutomataShader extends Shader {

    public AutomataShader() {
        super("automata", "position", "uv", "normal");
        start();
        uniform("texture1", 0);
        stop();
    }

    @Override
    public void shader() {
        uniform("vp", Camera.BUFFER);
        if (automata.mesh.binded()) {
            uniform("model", automata.buffer);
            glBindVertexArray(automata.mesh.vao);
            for (byte i = 0; i < attributes.length; i++) {
                glEnableVertexAttribArray(i);
            }
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, automata.material.texture);
            uniform("texture1", 0);
            glDrawElements(GL_TRIANGLES, automata.mesh.index, GL_UNSIGNED_INT, 0);
            for (int i = 0; i < attributes.length; i++) {
                glDisableVertexAttribArray(i);
            }
            glBindVertexArray(0);
        }
        for (Automata.Component component : automata.components) {
            uniform("model", component.buffer);
            glBindVertexArray(component.mesh.vao);
            for (byte i = 0; i < attributes.length; i++) {
                glEnableVertexAttribArray(i);
            }
            glDrawElements(GL_TRIANGLES, component.mesh.index, GL_UNSIGNED_INT, 0);
            for (int i = 0; i < attributes.length; i++) {
                glDisableVertexAttribArray(i);
            }
            glBindVertexArray(0);
        }
        glBindTexture(GL_TEXTURE_2D, 0);
    }

}
