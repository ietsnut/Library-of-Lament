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
        uniform("scale",        entity.mesh.collider.size);
        entity.mesh.collider.bind();
        glDrawElements(GL_LINES, entity.mesh.index, GL_UNSIGNED_INT, 0);
        entity.mesh.collider.unbind();
    }

}
