package shader;

import engine.Console;
import engine.Manager;
import engine.Scene;
import object.Camera;
import property.Entity;
import window.Window;

import static org.lwjgl.opengl.GL40.*;


public class AABBShader extends Shader<Entity> {

    public AABBShader(Window window) {
        super(window, "AABB", "position");
    }

    @Override
    protected void shader(Entity entity) {
        uniform("model",        entity.model.buffer());
        uniform("projection",   Camera.projection.buffer());
        uniform("view",         Camera.view.buffer());
        uniform("time",         Manager.time());
        uniform("scale",        entity.meshes[entity.state].collider.size);
        glBindVertexArray(entity.meshes[entity.state].collider.vao);
        for (byte i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        glDrawElements(GL_LINES, entity.meshes[entity.state].index, GL_UNSIGNED_INT, 0);
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

}
