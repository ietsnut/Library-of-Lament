package window;

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

public class Main extends Window {

    private EntityShader        entityShader;
    private FBOShader           fboShader;
    private AABBShader          aabbShader;
    private EnvironmentShader   environmentShader;
    private GUIShader           guiShader;

    // Bloom shaders
    private BrightnessShader    brightnessShader;
    private BloomShader         bloomShader;

    private Framebuffer framebuffer;
    private GUI gui;

    // Bloom FBOs - now using FBO class
    private Framebuffer brightnessFramebuffer;
    private Framebuffer pingPongFramebuffer1;
    private Framebuffer pingPongFramebuffer2;

    // Bloom settings
    private boolean enableBloom = true;
    private float bloomIntensity = 0.7f;  // Increased from 0.5f
    private float brightnessThreshold = 0.75f;  // Lowered from 0.8f to catch more areas

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

        // Initialize bloom shaders
        brightnessShader    = new BrightnessShader(this);
        bloomShader         = new BloomShader(this);

        int bloomWidth = width / 4;
        int bloomHeight = height / 4;

        brightnessFramebuffer = new Framebuffer(bloomWidth, bloomHeight)
                .attach(3, 16, false, true, true)
                .direct();

        pingPongFramebuffer1 = new Framebuffer(bloomWidth, bloomHeight)
                .attach(3, 16, false, true, true)
                .direct();

        pingPongFramebuffer2 = new Framebuffer(bloomWidth, bloomHeight)
                .attach(3, 16, false, true, true)
                .direct();

        brightnessShader.setBrightnessThreshold(brightnessThreshold);

        gui     = new GUI(new Material("ui"), new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f), 0);

        scene   = new Forest();

        framebuffer = new Framebuffer(width, height)
                .attach(1, 8, false, false, false)
                .depth(32, false, true, true)
                .direct();

        Camera.listen();

    }

    @Override
    public void draw() {
        // Render scene to main FBO (keep your original pipeline)
        fboShader.bind(framebuffer);
        if (Camera.intersecting != null &&
                Camera.intersecting.mesh != null &&
                Camera.intersecting.mesh.collider != null &&
                Camera.intersecting.mesh.collider.linked()) {
            aabbShader.render(Camera.intersecting);
        }
        environmentShader.render(scene);
        entityShader.render(scene);

        framebuffer.unbind();

        // Bloom pass
        if (enableBloom) {
            // Extract bright areas
            brightnessShader.extractBrightness(framebuffer, brightnessFramebuffer);

            // Ping-pong blur
            bloomShader.renderBloom(brightnessFramebuffer, pingPongFramebuffer1, pingPongFramebuffer2);

            // Add bloom texture to FBO shader for composite
            fboShader.setBloomTexture(pingPongFramebuffer1.textures[0]);
            fboShader.setBloomSettings(enableBloom, bloomIntensity);

        } else {
            fboShader.setBloomSettings(false, 0.0f);
        }

        // Use your original rendering method
        fboShader.render(framebuffer);

        // Render GUI
        gui.rotation += 0.001f;
        guiShader.render(gui);
    }

    @Override
    public void clear() {
        scene.unbind();
        framebuffer.unlink();
        if (brightnessFramebuffer != null) brightnessFramebuffer.unlink();
        if (pingPongFramebuffer1 != null) pingPongFramebuffer1.unlink();
        if (pingPongFramebuffer2 != null) pingPongFramebuffer2.unlink();
    }

    // Utility methods for runtime bloom control
    public void setBloomEnabled(boolean enabled) {
        this.enableBloom = enabled;
    }

    public void setBloomIntensity(float intensity) {
        this.bloomIntensity = Math.max(0.0f, intensity);
    }

    public void setBrightnessThreshold(float threshold) {
        this.brightnessThreshold = Math.max(0.0f, Math.min(1.0f, threshold));
        if (brightnessShader != null) {
            brightnessShader.setBrightnessThreshold(threshold);
        }
    }

}