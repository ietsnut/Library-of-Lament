package engine;

import game.Manager;
import game.Scene;
import object.Camera;
import property.Entity;

import static org.lwjgl.opengl.GL40.*;


public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        /*
        if (scene.active == null) {
            return;
        }
        uniform("model",        scene.active.model);
        uniform("projection",   Camera.projection);
        uniform("view",         Camera.view);
        uniform("time",         Manager.time() / 1000.0f);
        uniform("scale",        scene.active.meshes.getFirst().collider.size);
        render(scene.active);
*/
    }

    protected void render(Entity entity) {
        /*
        System.out.println("AABBShader.render");
        glBindVertexArray(entity.meshes.getFirst().collider.vao);
        for (byte i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        glDrawElements(GL_LINES, entity.meshes.getFirst().collider.indices.length, GL_UNSIGNED_INT, 0);
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);

         */
    }

}
