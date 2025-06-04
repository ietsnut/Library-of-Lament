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
import scene.Top;
import scene.Train;
import shader.AABBShader;
import shader.EntityShader;
import shader.EnvironmentShader;
import shader.FBOShader;

import static org.lwjgl.glfw.GLFW.*;

public class Main extends Window {

    private EntityShader entityShader;
    private FBOShader fboShader;
    private AABBShader aabbShader;

    private EnvironmentShader environmentShader;

    private FBO fbo;

    public static Scene scene;

    private static Scene test;

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

        fboShader = new FBOShader();
        entityShader = new EntityShader();
        //aabbShader = new AABBShader();

        fbo = new FBO(3);

        environmentShader = new EnvironmentShader();

        scene = new Forest();
        test = new Test();
        Resource.process();
        Camera.listen();
    }

    @Override
    public void draw() {
        fboShader.bind(fbo);
        /*
        if (Camera.intersecting != null &&
                Camera.intersecting.meshes[Camera.intersecting.state] != null &&
                Camera.intersecting.meshes[Camera.intersecting.state].collider != null &&
                Camera.intersecting.meshes[Camera.intersecting.state].collider.binded()) {
            aabbShader.render(Camera.intersecting);
        }*/
        environmentShader.render(test);
        entityShader.render(scene);
        fboShader.unbind(fbo);
        fboShader.render(fbo);
    }

}
