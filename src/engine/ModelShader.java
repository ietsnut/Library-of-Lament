package engine;

import game.Scene;
import org.joml.Math;
import object.*;

import static org.lwjgl.opengl.GL40.*;

public class ModelShader extends Shader {

    public ModelShader() {
        super("model", "position", "uv", "normal");
    }

    public void shader(Scene scene) {
        uniform("projection",       Renderer.projection);
        uniform("view",             Camera.view);
        uniform("lights",           Math.min(scene.lights.size(), LIGHTS));
        for (int i = 0; i < Math.min(scene.lights.size(), LIGHTS); i++) {
            uniform("lightPosition[" + i + "]",     scene.lights.get(i).position);
            uniform("lightAttenuation[" + i + "]",  scene.lights.get(i).attenuation);
            uniform("lightIntensity[" + i + "]",    scene.lights.get(i).intensity);
        }
        for (Entity model : scene.entities) {
            if (model.bound()) {
                render(model);
            }
        }
        if (scene.terrain.bound()) {
            render(scene.terrain);
        }
    }

    @Override
    protected void render(Entity entity) {
        uniform("model",        entity.model);
        super.render(entity);
    }

}
