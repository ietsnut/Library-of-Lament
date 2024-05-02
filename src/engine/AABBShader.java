package engine;

import content.Vase;
import game.Control;
import game.Game;
import game.Scene;
import object.Camera;
import object.Entity;
import property.Interactive;

import static org.lwjgl.glfw.GLFW.*;


public class AABBShader extends Shader {

    public AABBShader() {
        super("AABB", "position");
    }

    public void shader(Scene scene) {
        Entity entity = Entity.collides(30f, scene.entities);
        if (entity == null || !entity.bound() || !entity.collider.bound()) {
            return;
        }
        if (entity instanceof Interactive interactive && glfwGetMouseButton(Game.window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
            interactive.interact();
        }
        uniform("model",        entity.model);
        uniform("projection",   Camera.projection);
        uniform("view",         Camera.view);
        uniform("time",         Game.time() / 1000.0f);
        uniform("scale",        entity.collider.size);
        render(entity.collider);
    }
}
