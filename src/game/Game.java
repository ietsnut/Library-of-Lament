package game;

import engine.Renderer;
import object.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Game {

    ArrayList<Scene> scenes;
    Scene scene;

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int FPS_CAP = 70;
    private static final String TITLE = "Our First Display";

    public Game() {
        ContextAttribs attribs = new ContextAttribs(3, 2).withForwardCompatible(true).withProfileCore(true);
        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.create(new PixelFormat(), attribs);
            Display.setTitle(TITLE);
            Display.setVSyncEnabled(true);
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
        GL11.glViewport(0, 0, WIDTH, HEIGHT);

        Model model1 = new Model("1");
        model1.scale(20);
        Terrain terrain1 = new Terrain("1");
        Light light1 = new Light(new Vector3f (-185, 10, -293), new Vector3f(1, 0.01f, 0.002f));
        Sky sky1 = new Sky("1");

        Renderer renderer = new Renderer();
        Mouse.setGrabbed(true);
        while (!Display.isCloseRequested()) {
            Camera.move(terrain1);
            Vector3f pos = new Vector3f(Camera.position);
            pos.translate(0, 10, 0);
            light1.position = pos;
            renderer.render(List.of(light1), List.of(model1), terrain1, sky1);
            Display.sync(FPS_CAP);
            Display.update();
        }
        renderer.clean();
        Display.destroy();
    }

}
