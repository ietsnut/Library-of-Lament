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

    private FBO fbo;
    private GUI gui;

    // Bloom FBOs
    private BloomFBO brightnessFBO;
    private BloomFBO pingPongFBO1;
    private BloomFBO pingPongFBO2;

    // Bloom settings
    private boolean enableBloom = true;
    private float bloomIntensity = 0.7f;  // Increased from 0.5f
    private float brightnessThreshold = 0.75f;  // Lowered from 0.8f to catch more areas
    private int blurPasses = 16;  // Increased from 10 for wider spread

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

        fbo     = new FBO(1, width, height);

        // Create bloom FBOs - using quarter resolution for performance
        int bloomWidth = width / 4;
        int bloomHeight = height / 4;
        brightnessFBO   = new BloomFBO(bloomWidth, bloomHeight);
        pingPongFBO1    = new BloomFBO(bloomWidth, bloomHeight);
        pingPongFBO2    = new BloomFBO(bloomWidth, bloomHeight);

        // Configure bloom settings
        brightnessShader.setBrightnessThreshold(brightnessThreshold);
        bloomShader.setBlurPasses(blurPasses);

        gui     = new GUI(new Material("ui"), new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f), 0);

        scene   = new Forest();

        NewFBO newFBO = (NewFBO) new NewFBO(width, height)
                .attach(1, 8, false, false, false)
                .depth(32, false, true, true)
                .queue();

        Resource.process();
        Camera.listen();
    }

    @Override
    public void draw() {
        // Render scene to main FBO (keep your original pipeline)
        fboShader.bind(fbo);
        if (Camera.intersecting != null &&
                Camera.intersecting.meshes[Camera.intersecting.state] != null &&
                Camera.intersecting.meshes[Camera.intersecting.state].collider != null &&
                Camera.intersecting.meshes[Camera.intersecting.state].collider.linked()) {
            aabbShader.render(Camera.intersecting);
        }
        environmentShader.render(scene);
        entityShader.render(scene);

        fboShader.unbind(fbo);

        // Bloom pass
        if (enableBloom) {
            // Extract bright areas
            brightnessShader.extractBrightness(fbo, brightnessFBO);

            // Ping-pong blur
            bloomShader.renderBloom(brightnessFBO, pingPongFBO1, pingPongFBO2);

            // Add bloom texture to FBO shader for composite
            fboShader.setBloomTexture((blurPasses % 2 == 0) ? pingPongFBO1.colorTexture : pingPongFBO2.colorTexture);
            fboShader.setBloomSettings(enableBloom, bloomIntensity);

        } else {
            fboShader.setBloomSettings(false, 0.0f);
        }

        // Use your original rendering method
        fboShader.render(fbo);

        // Render GUI
        gui.rotation += 0.001f;
        guiShader.render(gui);
    }

    @Override
    public void clear() {
        fbo.unlink();
        if (brightnessFBO != null) brightnessFBO.unlink();
        if (pingPongFBO1 != null) pingPongFBO1.unlink();
        if (pingPongFBO2 != null) pingPongFBO2.unlink();
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

    public void setBlurPasses(int passes) {
        this.blurPasses = Math.max(1, passes);
        if (bloomShader != null) {
            bloomShader.setBlurPasses(passes);
        }
    }
}