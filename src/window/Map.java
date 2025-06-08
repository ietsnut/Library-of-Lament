package window;

import org.joml.Vector2f;
import property.GUI;
import resource.Material;
import resource.Mesh;
import shader.GUIShader;

import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;

public class Map extends Window {

    private GUIShader guiShader;

    public GUI gui;

    public Map(int size) {
        super(size, size);
        gui = new GUI(new Material("wall"), new Vector2f(0.5f, 0.5f), new Vector2f(0.3f, 0.3f));
        glfwSetWindowPos(handle, 50, 50);
    }

    @Override
    public void setup() {
        guiShader = new GUIShader();
    }

    @Override
    public void draw() {
        guiShader.render(gui);
    }
}
