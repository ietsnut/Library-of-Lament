package engine;

import game.Game;
import game.Scene;
import object.*;
import org.lwjgl.util.vector.Matrix4f;


import java.util.List;

public class ModelShader extends Shader {

    public ModelShader() {
        super("model", "position", "uv", "normal");
    }

    public void shader(Scene scene) {
        uniform("projection",       Renderer.projection());
        uniform("view",             Camera.view);
        for (int i = 0; i < Light.ALL.size(); i++) {
            uniform("lightPosition[" + i + "]",     scene.lights.get(i).position);
            uniform("lightAttenuation[" + i + "]",  scene.lights.get(i).attenuation);
            uniform("lightIntensity[" + i + "]",    scene.lights.get(i).intensity);
        }
        for (Entity model : scene.entities) {
            uniform("modelTexture", 0);
            uniform("noiseTexture", 1);
            uniform("model",        model.transformation.model());
            uniform("frame",        model.frame);
            uniform("frames",       model.textures.getFirst().frames);
            render(model);
        }
    }

}
