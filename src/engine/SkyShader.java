package engine;

import game.Scene;
import object.Camera;
import org.joml.Matrix4f;

public class SkyShader extends Shader {

    public SkyShader() {
        super("sky", "position", "uv");
    }

    public void shader(Scene scene) {
        /*
        glDepthMask(false);
        uniform("projection",   Renderer.projection);
        uniform("view",         view());
        if (scene.sky == null) {
            return;
        }
        for (int i = scene.sky.layers.size() - 1; i >= 0; i--) {
            Sky.Layer layer = scene.sky.layers.get(i);
            uniform("model",    layer.model());
            //uniform("tile",     i);
            render(layer);
        }
        glDepthMask(true);*/
    }

    private Matrix4f view() {
        Matrix4f matrix = new Matrix4f(Camera.view);
        matrix.m30(0);
        matrix.m31(0);
        matrix.m32(0);
        return matrix;
    }

}
