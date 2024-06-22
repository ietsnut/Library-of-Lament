package engine;

import game.Game;
import game.Scene;
import object.Camera;
import property.Entity;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;


public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        if (scene.active == null) {
            return;
        }
        uniform("model",        scene.active.model);
        uniform("projection",   Camera.projection);
        uniform("view",         Camera.view);
        uniform("time",         Game.time() / 1000.0f);
        uniform("scale",        scene.active.meshes.get(scene.active.mesh).collider.size);
        render(scene.active);
    }

    @Override
    protected void render(Entity entity) {
        System.out.println("AABBShader.render");
        glBindVertexArray(entity.meshes.get(entity.mesh).collider.vao);
        for (byte i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        glDrawElements(GL_LINES, entity.meshes.get(entity.mesh).collider.indices.length, GL_UNSIGNED_INT, 0);
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

}
