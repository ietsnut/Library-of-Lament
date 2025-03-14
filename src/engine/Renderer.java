package engine;

import shader.AutomataShader;
import shader.FBOShader;
import automata.Balancer;
import object.Automata;

import java.util.ArrayList;

public class Renderer {

    private static AutomataShader automataShader;
    private static FBOShader fboShader;

    public static ArrayList<Automata> automatas = new ArrayList<>();
    public static Automata automata;

    public static void init() {
        fboShader    = new FBOShader();
        automataShader = new AutomataShader();
        automatas.add(new Balancer());
        automata = automatas.getFirst();
    }

    private static float worldYaw   = 0f;
    private static float worldPitch = 90f;

    public static void render() {

        float dx = Control.dx() * 0.5f;
        float dy = Control.dy() * 0.5f;

        worldYaw += dx;
        worldPitch += dy;

        worldPitch = (worldPitch % 360f + 360f) % 360f;
        worldYaw   = (worldYaw   % 360f + 360f) % 360f;

        automata.orientation.identity()
                .rotateX((float) Math.toRadians(worldPitch))
                .rotateY((float) Math.toRadians(worldYaw));

        automata.update();

        fboShader.bind();
        automataShader.render();
        fboShader.unbind();
        fboShader.render();
    }

}
