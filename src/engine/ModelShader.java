package engine;

import game.Game;
import game.Scene;
import object.*;


import java.util.List;

public class ModelShader extends Shader {

    public ModelShader() {
        super("model", "position", "uv", "normal");
        start();
        uniform("projection",       Renderer.projection());
        uniform("modelTexture",     0);
        stop();
    }

    public void shader(Scene scene) {
        uniform("view",             Camera.view);
        for (int i = 0; i < Light.ALL.size(); i++) {
            uniform("lightPosition[" + i + "]",     scene.lights.get(i).position);
            uniform("lightAttenuation[" + i + "]",  scene.lights.get(i).attenuation);
            uniform("lightIntensity[" + i + "]",    scene.lights.get(i).intensity);
        }
        for (Entity model : scene.entities) {
            uniform("model",        model.transformation.model());
            render(model);
        }
    }

}
