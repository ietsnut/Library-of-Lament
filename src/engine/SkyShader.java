package engine;

import game.Scene;
import object.Sky;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

public class SkyShader extends Shader {

    public SkyShader() {
        super("sky", "position", "uv");
        start();
        uniform("modelTexture", 0);
        uniform("projection",   Renderer.projection());
        stop();
    }

    public void shader(Scene scene) {
        glDepthMask(false);
        uniform("view",         view());
        for (int i = scene.sky.layers.size() - 1; i >= 0; i--) {
            Sky.Layer layer = scene.sky.layers.get(i);
            uniform("model",    layer.transformation.model());
            render(layer);
        }
        glDepthMask(true);
    }

    private Matrix4f view() {
        Matrix4f matrix = new Matrix4f(Renderer.camera.transformation.view());
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        return matrix;
    }

}
