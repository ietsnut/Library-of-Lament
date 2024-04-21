package game;

import engine.*;
import object.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import property.Load;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 1200;

    private final List<Scene> scenes = new ArrayList<>();
    public static Scene scene;

    public Game() {

        ContextAttribs attribs = new ContextAttribs(4, 3).withForwardCompatible(true).withProfileCore(true);
        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.create(new PixelFormat(), attribs);
            Display.setVSyncEnabled(true);
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
        GL11.glViewport(0, 0, WIDTH, HEIGHT);

        Renderer renderer = new Renderer();

        scenes.add(new Scene());
        scene = scenes.getFirst();

       // Window window1 = new Window(Texture.load("texture/1"), 1000, 1000);

        try {
            Display.makeCurrent();
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
        Mouse.setGrabbed(true);
        int fps = 0;
        long lastFrameTime = (Sys.getTime() * 1000) / Sys.getTimerResolution();;
        while (!Display.isCloseRequested()) {
            Load load = Load.process();
            if (load != null) {
                System.out.println(load);
            }
            if (load instanceof Terrain terrain) {
                scene.terrain = terrain;
            } else if (load instanceof Entity entity) {
                scene.entities.add(entity);
            }
            if ((Sys.getTime() * 1000) / Sys.getTimerResolution() - lastFrameTime > 1000) {
                Display.setTitle("FPS: " + fps);
                fps = 0;
                lastFrameTime += 1000;
            }
            fps++;
            renderer.render(scene);
            Display.update();
        }
        renderer.clean();
        Display.destroy();

    }

}
