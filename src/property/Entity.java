package property;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import property.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Entity {

    public final Vector3f position  = new Vector3f();
    public final Vector3f rotation  = new Vector3f();
    public float scale              = 1;

    public final Matrix4f   model       = new Matrix4f();

    public List<Component>  components  = new ArrayList<>();

    public List<Material>   materials   = new ArrayList<>();
    public List<Mesh>       meshes      = new ArrayList<>();
    public List<Music>      music       = new ArrayList<>();

    public final String     type        = this.getClass().getSimpleName().toLowerCase();
    public final String     name;

    public int states;

    public Entity(String name) {
        this.name   = name;
        this.states = 1;
    }

    public Entity(String name, int states) {
        this.name   = name;
        this.states = states;
    }

    @Override
    public String toString() {
        return "<" + type + "> [" + type + "] : " + position.x + ", " + position.y + ", " + position.z + " : " + rotation.x + ", " + rotation.y + ", " + rotation.z + " : " + scale;
    }

    private Matrix4f model() {
        return model.identity().translate(position.x, position.y, position.z).scale(scale).rotate((float) Math.toRadians(rotation.y * 5), new Vector3f(0, 1, 0)).rotate((float) Math.toRadians(rotation.x * 5), new Vector3f(1, 0, 0)).rotate((float) Math.toRadians(rotation.z * 5), new Vector3f(0, 0, 1));
    }

    public void remodel() {
        this.model.set(model());
    }

}
