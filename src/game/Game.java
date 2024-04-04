package game;

import engine.Renderer;
import object.*;
import object.Window;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Game {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 1280;

    private final List<Scene> scenes = new ArrayList<>();
    //public static Window stone;

    public Game() {

        ContextAttribs attribs = new ContextAttribs(3, 2).withForwardCompatible(true).withProfileCore(true);
        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.create(new PixelFormat(), attribs);
            Display.setVSyncEnabled(true);
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
        GL11.glViewport(0, 0, WIDTH, HEIGHT);

        scenes.add(new Scene());

        //Texture windowTexture = new Texture("resource/texture/stone.png");
        //stone = new Window(windowTexture.image, WIDTH/3, HEIGHT/3);

        Renderer renderer = new Renderer();
        Mouse.setGrabbed(true);

        int fps = 0;
        long lastFrameTime = (Sys.getTime() * 1000) / Sys.getTimerResolution();;
        while (!Display.isCloseRequested()) {
            //sky1.rotation.y += 0.01f;
            if ((Sys.getTime() * 1000) / Sys.getTimerResolution() - lastFrameTime > 1000) {
                Display.setTitle("FPS: " + fps);
                fps = 0; // Reset the FPS counter
                lastFrameTime += 1000; // Move to the next second
            }
            fps++;
            scenes.getFirst().update();
            renderer.render(scenes.getFirst());
            Display.update();
        }

        renderer.clean();
        Display.destroy();

    }

}
