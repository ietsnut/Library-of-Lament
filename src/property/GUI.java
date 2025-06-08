package property;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import resource.Material;
import resource.Mesh;

public class GUI {

    public final Material material;
    public final Vector2f position;
    public final Vector2f scale;

    public GUI(Material material, Vector2f position, Vector2f scale) {
        this.material = material;
        this.position = position;
        this.scale = scale;
    }

    public void unbind() {
        this.material.unbind();
    }

}
