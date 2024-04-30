package engine;

import game.Scene;
import org.joml.Math;
import object.*;

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
        uniform("modelTexture", 0);
        for (Entity model : scene.entities) {
            render(model);
        }
        //render(scene.terrain);
    }

    @Override
    protected void render(Entity entity) {
        if (entity != null) {
            uniform("model",        entity.model);
            super.render(entity);
        }
    }
}
