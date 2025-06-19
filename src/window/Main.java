package window;

import engine.Console;
import engine.Control;
import engine.Manager;
import engine.Scene;
import object.Camera;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import property.GUI;
import resource.FBO;
import org.lwjgl.opengl.GL;
import resource.Material;
import resource.Mesh;
import resource.Resource;
import scene.Dungeon;
import scene.Forest;
import shader.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main extends Window {

    private EntityShader        entityShader;
    private FBOShader           fboShader;
    private AABBShader          aabbShader;
    private EnvironmentShader   environmentShader;

    private GUIShader           guiShader;

    private FBO fbo;
    private GUI gui;

    public static Scene scene;

    public Main(int size) {
        super(size, size);
        Control.listen(handle);
        makeContextCurrent();
        GL.createCapabilities();
        capabilities = GL.getCapabilities();
        glfwSetWindowPos(handle, ((int) Manager.WIDTH - size) / 2, ((int) Manager.HEIGHT - size) / 2);
    }

    @Override
    public void setup() {
        fboShader           = new FBOShader(this);
        entityShader        = new EntityShader(this);
        aabbShader          = new AABBShader(this);
        environmentShader   = new EnvironmentShader(this);
        guiShader           = new GUIShader(this);

        fbo     = new FBO(1, width, height);

        gui     = new GUI(new Material("ui"), new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f), 0);

        scene   = new Forest();
        Resource.process();
        Camera.listen();
    }

    @Override
    public void draw() {
        fboShader.bind(fbo);
        if (Camera.intersecting != null &&
                Camera.intersecting.meshes[Camera.intersecting.state] != null &&
                Camera.intersecting.meshes[Camera.intersecting.state].collider != null &&
                Camera.intersecting.meshes[Camera.intersecting.state].collider.binded()) {
            aabbShader.render(Camera.intersecting);
        }
        environmentShader.render(scene);
        entityShader.render(scene);
        fboShader.unbind(fbo);
        fboShader.render(fbo);
        gui.rotation += 0.001f;
        guiShader.render(gui);
    }

    @Override
    public void clear() {
        fbo.unbind();
    }

}
