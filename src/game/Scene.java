package game;

import content.Tree;
import object.*;
import org.joml.Vector3f;
import property.Transformation;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

public class Scene extends Thread {

    public Terrain      terrain;
    public Sky          sky;
    public List<Entity> entities    = new ArrayList<>();

    public List<Light>  lights      = new ArrayList<>();

    public Scene() {
        start();
    }

    public void run() {

        //Terrain terrain = (Terrain) new Terrain("test").texture(new Texture("terrain", "test"));
        //new Sky("1", 3);

        /*
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
            model1.position(x, 0, z);
        }*/

        Texture vase = new Texture("texture", "vase");

        for (int i = 0; i < 30; i+=3) {
            Model vasee = (Model) new Model("vase").texture(vase);
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

        Model model5 = (Model) new Model("sewer").texture(new Texture("texture", "sewer"));
        model5.scale(1f);
        model5.translate(Transformation.Axis.Y, -1);

        Billboard billboard = (Billboard) new Billboard("1").texture(new Texture("texture", "1"));
        billboard.scale(1f/2);
        billboard.position(10, 0, 1.5f);

        Light light1 = new Light(new Vector3f(-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);

        //Light light2 = new Light(new Vector3f(-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 10f);
        //light2.position = new Vector3f(model2.position).translate(0, 3, 0);
        //lights.add(light2);

        while (!glfwWindowShouldClose(Game.window)) {
            update();
        }

    }

    public void update() {
        lights.getFirst().position = new Vector3f(Camera.transformation.position);
    }

}
