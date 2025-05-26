package shader;

import engine.Console;
import engine.Manager;
import engine.Scene;
import object.Camera;
import property.Entity;

import static org.lwjgl.opengl.GL40.*;


public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        if (Camera.intersecting == null ||
                Camera.intersecting.meshes[Camera.intersecting.state] == null ||
                Camera.intersecting.meshes[Camera.intersecting.state].collider == null ||
                !Camera.intersecting.meshes[Camera.intersecting.state].collider.binded()) return;
        render(Camera.intersecting);
    }

    protected void render(Entity entity) {
        uniform("model",        entity.model.buffer());
        uniform("projection",   Camera.projection.buffer());
        uniform("view",         Camera.view.buffer());
        uniform("time",         Manager.time() / 1000.0f);
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
