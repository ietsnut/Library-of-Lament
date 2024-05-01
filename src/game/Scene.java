package game;

import content.Terrain;
import content.Vase;
import object.*;
import org.joml.Vector3f;
import property.Transformation;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

public class Scene extends Thread {

    public Sky          sky;

    public final List<Entity> entities    = new ArrayList<>();

    public List<Light>  lights      = new ArrayList<>();

    public Scene() {

        Texture vase = new Texture("texture", "vase");

        for (int i = 0; i < 30; i+=3) {
            Vase vasee = (Vase) new Vase("vase").texture(vase);
            float scale = Math.random() > 0.5 ? 1f/24 : 1f/20;
            vasee.scale(scale);
            float x = (float) (Math.random() * i);
            float z = 1.5f;
            if (Math.random() > 0.5) {
                x = -x;
            }
            if (Math.random() > 0.5) {
                z = -z;
            }
            vasee.position(x, 0, z);
        }

        Model model3 = (Model) new Model("slotmachine").texture(new Texture("texture", "slotmachine0"));
        model3.scale(1f/8);
        model3.position(-10, 0, -1.5f);

        new Terrain("sewer");

        Billboard billboard = (Billboard) new Billboard("1").texture(new Texture("texture", "1"));
        billboard.scale(1f/2);
        billboard.position(10, 0, 1.5f);

        Light light1 = new Light(new Vector3f(-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);

        start();
    }

    @Override
    public void run() {
        while (!glfwWindowShouldClose(Game.window)) {
            update();
        }
    }

    public void update() {
        lights.getFirst().position = new Vector3f(Camera.transformation.position);
    }

    public void render() {
        for (Entity entity : entities) {
            if (entity instanceof Vase vase) {
                vase.rotate(Transformation.Axis.Y, 0.05f);
            }
        }
    }

    public Terrain terrain() {
        for (Entity entity : entities) {
            if (entity instanceof Terrain terrain) {
                return terrain;
            }
        }
        return null;
    }

}
