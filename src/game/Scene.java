package game;

import content.Tree;
import engine.Renderer;
import object.*;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import property.Load;
import property.Transformation;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glGetError;

public class Scene extends Thread {

    public Terrain      terrain;
    public Sky          sky;
    public List<Entity> entities    = new ArrayList<>();

    public List<Light>  lights      = new ArrayList<>();

    public Scene() {
        start();
    }

    public void run() {

        Terrain terrain = new Terrain("test", 1);
        //new Sky("1", 3);

        Texture tree1 = new Texture("texture", "tree1");
        Texture tree2 = new Texture("texture", "tree2");
        for (int i = 0; i < 20; i+=2) {
            Tree model1;
            if (Math.random() > 0.5) {
                model1 = (Tree) new Tree("tree").texture(tree1);
            } else {
                model1 = (Tree) new Tree("tree").texture(tree2);
            }
            model1.scale(5f);
            float x = (float) (Math.random() * i);
            float z = (float) (Math.random() * i);
            if (Math.random() > 0.5) {
                x = -x;
            }
            if (Math.random() > 0.5) {
                z = -z;
            }
            model1.position(x, terrain.height(x, z), z);
        }

        Model model3 = (Model) new Model("slotmachine").texture(new Texture("texture", "slotmachine0"));
        model3.scale(0.25f);
        model3.position(10, terrain.height(10, 10), 10);

        Model model4 = (Model) new Model("vendingmachine").texture(new Texture("texture", "vendingmachine"));
        model4.scale(0.5f);
        model4.position(15, terrain.height(15, 15), 15);

        Billboard billboard = (Billboard) new Billboard("1").texture(new Texture("texture", "1"));
        billboard.position(20, terrain.height(20, 20), 20);

        Light light1 = new Light(new Vector3f (-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 10f);
        lights.add(light1);

        //Light light2 = new Light(new Vector3f(-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 10f);
        //light2.position = new Vector3f(model2.position).translate(0, 3, 0);
        //lights.add(light2);

        while (!Display.isCloseRequested()) {
            update();
        }

    }

    public void update() {
        lights.getFirst().position = new Vector3f(Camera.transformation.position);
    }

}
