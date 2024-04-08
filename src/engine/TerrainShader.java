package engine;

import game.Game;
import game.Scene;
import object.Camera;
import object.Light;
import object.Model;
import object.Terrain;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.*;

public class TerrainShader extends Shader {

    public TerrainShader() {
        super("terrain", "position", "uv", "normal");
    }

    public void shader(Scene scene) {
        uniform("tiling",       40f);
        uniform("projection",   Renderer.projection());
        uniform("view",         Camera.view);
        uniform("model",        scene.terrain.transformation.model());
        uniform("terrainTexture",       0);
        uniform("blendTexture",         1);
        for (int i = 0; i < Light.ALL.size(); i++) {
            uniform("lightPosition[" + i + "]",     scene.lights.get(i).position);
            uniform("lightAttenuation[" + i + "]",  scene.lights.get(i).attenuation);
            uniform("lightIntensity[" + i + "]",    scene.lights.get(i).intensity);
        }
        render(scene.terrain);
    }

}
