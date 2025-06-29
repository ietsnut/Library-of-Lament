package window;

import engine.Console;
import engine.Control;
import engine.Manager;
import engine.Scene;
import object.Camera;
import org.joml.Vector2f;
import resource.*;
import property.GUI;
import org.lwjgl.opengl.GL;
import scene.Forest;
import shader.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;

public class Main extends Window {

    private EntityShader        entityShader;
    private FBOShader           fboShader;
    private AABBShader          aabbShader;
    private EnvironmentShader   environmentShader;
    private GUIShader           guiShader;

    // Bloom shader (brightness extraction removed)
    private BloomShader         bloomShader;

    private Framebuffer framebuffer;
    private GUI gui;

    // Bloom FBOs
    private Framebuffer pingPongFramebuffer1;
    private Framebuffer pingPongFramebuffer2;

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

        bloomShader         = new BloomShader(this);

        int bloomWidth = width / 4;
        int bloomHeight = height / 4;

        framebuffer = new Framebuffer(width, height)
                .attach(1, 8, false, false, false)      // Attachment 0: Single channel for main scene
                .attach(3, 16, false, true, true)       // Attachment 1: RGB16F for bright pixels
                .depth(32, false, true, true)           // 32-bit floating point depth
                .clearColor(0.0f, 0.0f, 0.0f, 0.0f)
                .clearDepth(1.0f)
                .autoClear(true)
                .depthTest(true)
                .depthFunc(GL_LESS)
                .depthMask(true)
                .depthRange(0.0, 1.0)
                .blend(false)
                .direct();

        pingPongFramebuffer1 = new Framebuffer(bloomWidth, bloomHeight)
                .attach(3, 16, false, true, true)
                .clearColor(0.0f, 0.0f, 0.0f, 0.0f)
                .autoClear(true)
                .depthTest(false)
                .blend(true)
                .blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                .direct();

        pingPongFramebuffer2 = new Framebuffer(bloomWidth, bloomHeight)
                .attach(3, 16, false, true, true)
                .clearColor(0.0f, 0.0f, 0.0f, 0.0f)
                .autoClear(true)
                .depthTest(false)
                .blend(true)
                .blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                .direct();

        gui = new GUI(new Material("ui"), new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f), 0);
        scene = new Forest();

        Camera.listen();
    }

    @Override
    public void draw() {

        framebuffer.bind();

        if (Camera.intersecting != null &&
                Camera.intersecting.mesh != null &&
                Camera.intersecting.mesh.collider != null &&
                Camera.intersecting.mesh.collider.linked()) {
            aabbShader.render(Camera.intersecting);
        }
        environmentShader.render(scene);
        entityShader.render(scene);

        framebuffer.unbind();

        // Apply blur to bright pixels (using attachment 1 from main framebuffer)
        bloomShader.render(new Framebuffer[]{framebuffer, pingPongFramebuffer1, pingPongFramebuffer2});

        // Render final composite to screen
        fboShader.render(new Framebuffer[]{framebuffer, pingPongFramebuffer1});

        // Render GUI on top
        gui.rotation += 0.001f;
        guiShader.render(gui);
    }

    @Override
    public void clear() {
        scene.unbind();
        framebuffer.unlink();
        pingPongFramebuffer1.unlink();
        pingPongFramebuffer2.unlink();
    }
}