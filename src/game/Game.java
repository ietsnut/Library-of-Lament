package game;

import engine.Renderer;
import object.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
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
            throw new RuntimeException(e);
        }
        GL11.glViewport(0, 0, WIDTH, HEIGHT);

        Terrain terrain1 = new Terrain("1");
        Model model1 = new Model("cottage");
        model1.position(0, terrain1.getHeightOfTerrain(0, 0), 0);


        //Decal billboard = new Decal("1");
        //billboard.scale(3);
        //light1 = new Light(new Vector3f (-185f, 10f, -293f), new Vector3f(1.0f, 0.14f, 0.007f));
        Light light1 = new Light(new Vector3f (-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 10);
        Sky sky1 = new Sky("1");

        Renderer renderer = new Renderer();
        Mouse.setGrabbed(true);

        int fps = 0;
        long lastFrameTime = (Sys.getTime() * 1000) / Sys.getTimerResolution();;
        while (!Display.isCloseRequested()) {
            //sky1.rotation.y += 0.01f;
            if ((Sys.getTime() * 1000) / Sys.getTimerResolution() - lastFrameTime > 1000) { // One second passed
                Display.setTitle("FPS: " + fps);
                fps = 0; // Reset the FPS counter
                lastFrameTime += 1000; // Move to the next second
            }
            fps++;
            Vector3f pos = new Vector3f(Camera.position);
            pos.y = terrain1.getHeightOfTerrain(pos.x, pos.z) + 2f;
            light1.position = pos;
            renderer.render(List.of(light1), List.of(model1), terrain1, sky1);
            Display.update();
        }

        renderer.clean();
        Display.destroy();

    }

}
