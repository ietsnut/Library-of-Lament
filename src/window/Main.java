package window;

import engine.Console;
import engine.Control;
import engine.Manager;
import engine.Scene;
import object.Camera;
import org.lwjgl.BufferUtils;
import resource.FBO;
import org.lwjgl.opengl.GL;
import resource.Mesh;
import resource.Resource;
import scene.Forest;
import scene.Test;
import shader.AABBShader;
import shader.EntityShader;
import shader.EnvironmentShader;
import shader.FBOShader;

import static org.lwjgl.glfw.GLFW.*;

public class Main extends Window {

    private EntityShader        entityShader;
    private FBOShader           fboShader;
    private AABBShader          aabbShader;
    private EnvironmentShader   environmentShader;

    private FBO fbo;

    public static Scene background;
    public static Scene foreground;

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
        fboShader           = new FBOShader();
        entityShader        = new EntityShader();
        aabbShader          = new AABBShader();
        environmentShader   = new EnvironmentShader();
        fbo     = new FBO(1, width, height);
        background   = new Forest();
        foreground   = new Test();
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
        environmentShader.render(background);
        entityShader.render(foreground);
        fboShader.unbind(fbo);
        fboShader.render(fbo);
    }

}
