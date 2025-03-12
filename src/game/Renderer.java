package game;

import engine.EntityShader;
import engine.FBOShader;
import entities.Tower;
import property.Entity;

import java.util.ArrayList;

public class Renderer {

    private static EntityShader entityShader;
    private static FBOShader fboShader;

    public static ArrayList<Entity> ENTITIES = new ArrayList<>();

    public static void init() {
        fboShader    = new FBOShader();
        entityShader = new EntityShader();

        Tower tower = new Tower("device");
        tower.rotation.y = 45;
        //tower.position.y = -40;
        tower.scale.set(3);
        tower.update();
        ENTITIES.add(tower);

    }

    public static void render() {
        ENTITIES.getFirst().rotation.y += Serial.get(0, 0) ? 0.5f : -0.5f;
        ENTITIES.getFirst().update();
        fboShader.bind();
        entityShader.render();
        fboShader.unbind();
        fboShader.render();
    }

}
