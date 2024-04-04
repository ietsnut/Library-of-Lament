package game;

import engine.Renderer;
import object.*;
import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;
import property.Axis;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    public Terrain      terrain;
    public Sky          sky;
    public List<Entity> entities;
    public List<Light>  lights;

    public Scene () {

        entities    = new ArrayList<>();
        lights      = new ArrayList<>();

        terrain     = new Terrain("1");
        sky         = new Sky("1", 3);

        Model model1 = new Model("cottage");
        model1.transformation.position(0, terrain.getHeightOfTerrain(0, 0), 0);
        entities.add(model1);

        Model model2 = new Model("tree1", "tree1");
        model2.transformation.position(30, terrain.getHeightOfTerrain(30, 30), 30);
        entities.add(model2);

        Billboard billboard = new Billboard("1");
        billboard.transformation.scale(3);
        billboard.transformation.position(20, terrain.getHeightOfTerrain(20, 20) + 1, 20);
        entities.add(billboard);

        Light light1 = new Light(new Vector3f (-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 10);
        lights.add(light1);

        Light light2 = new Light(new Vector3f(-185f, 10f, -293f), new Vector3f(1.0f, 0.7f, 0.07f), 10);
        light2.position = new Vector3f(model2.transformation.position).translate(0, 3, 0);
        lights.add(light2);

    }

    public void update() {
        lights.getFirst().position = new Vector3f(Renderer.camera.transformation.position);
        for (Entity entity : entities) {
            if (entity instanceof Model model) {
                model.transformation.rotation(Sys.getTime() / 1000f);
            }
        }
    }

}
