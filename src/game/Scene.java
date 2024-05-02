package game;

import content.Character;
import content.Terrain;
import content.Vase;
import engine.Renderer;
import object.*;
import org.joml.Vector3f;
import property.Load;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import static property.Transformation.*;

public class Scene extends Thread {

    public List<Light>  lights      = new ArrayList<>();
    public List<Entity> entities    = new ArrayList<>();
    public Terrain      terrain;

    public Scene() {

        terrain = new Terrain("sewer");

        for (int i = 0; i < 30; i+=3) {
            Vase vase = new Vase("vase");
            vase.scale = MICRO;
            float x = (float) (Math.random() * i);
            float z = 1.5f;
            if (Math.random() > 0.5) {
                x = -x;
            }
            if (Math.random() > 0.5) {
                z = -z;
            }
            vase.position.set(x, 0, z);
            entities.add(vase);
        }

        Character character = new Character("1");
        character.scale = DECI;
        character.position.set(10, 0, 1.5f);
        entities.add(character);

        Light light1 = new Light(new Vector3f(-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 2f);
        lights.add(light1);

        start();

    }

    @Override
    public void run() {
        while (!glfwWindowShouldClose(Game.window)) {
            lights.getFirst().position = new Vector3f(Camera.transformation.position);
            for (Entity entity : entities) {
                entity.remodel();
            }
        }
    }

}
