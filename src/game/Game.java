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

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 1280;
    private static final int FPS_CAP = 70;
    private static final String TITLE = "Our First Display";

    public static Scene scene;

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

        Terrain terrain1 = new Terrain("1");
        Model model1 = new Model("cottage");
        model1.position(0, terrain1.getHeightOfTerrain(0, 0), 0);
        Light light1 = new Light(new Vector3f (-185f, 10f, -293f), new Vector3f(1.0f, 0.14f, 0.07f));
        Light light2 = new Light(new Vector3f (-185f, 10f, -293f), new Vector3f(1.0f, 0.14f, 0.07f));
        light2.position = new Vector3f(model1.position).translate(0, 1, 0);
        Sky sky1 = new Sky("1");

        Renderer renderer = new Renderer();
        Mouse.setGrabbed(true);

        while (!Display.isCloseRequested()) {
            sky1.rotation.y += 0.01f;
            light1.position = new Vector3f(Camera.position);
            renderer.render(List.of(light1, light2), List.of(model1), terrain1, sky1);
            Display.sync(FPS_CAP);
            Display.update();
        }

        renderer.clean();
        Display.destroy();

    }

}
