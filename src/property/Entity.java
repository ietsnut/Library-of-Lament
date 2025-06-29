package property;

import object.Matrix;
import resource.Material;
import resource.Mesh;
import org.joml.Vector3f;
import resource.Resource;

import java.util.HashMap;
import java.util.Map;

public class Entity {

    public final String type = this.getClass().getSimpleName().toLowerCase();

    public final Vector3f position  = new Vector3f(0);
    public final Vector3f rotation  = new Vector3f(0);
    public final Vector3f scale     = new Vector3f(1);

    public final Matrix   model     = new Matrix();

    public final Material material;
    public final Mesh     mesh;

    private final String  name;

    public final int states;
    public int state = 0;

    private static final Map<String, Mesh>      meshCache       = new HashMap<>();
    private static final Map<String, Material>  materialCache   = new HashMap<>();

    public Entity(String name, int states) {
        this.name       = name;
        this.states     = states;
        this.mesh       = meshCache.computeIfAbsent(type + name, k -> new Mesh(name, type));
        this.material   = materialCache.computeIfAbsent(type + name + states, k -> new Material(name, type, states));
    }

    public Entity(String name, int states, Mesh mesh) {
        this.name       = name;
        this.states     = states;
        this.mesh       = mesh;
        this.material   = materialCache.computeIfAbsent(type + name + states, k -> new Material(name, type, states));
    }

    public Entity(String name, Mesh mesh) {
        this(name, 1, mesh);
    }

    public Entity(String name) {
        this(name, 1);
    }

    @Override
    public String toString() {
        return "< " + type + " > [state: " + (state + 1) + " of " + states + "]: " + position.x + ", " + position.y + ", " + position.z + " : " + rotation.x + ", " + rotation.y + ", " + rotation.z + " : " + scale;
    }

    public static final Vector3f X = new Vector3f(1, 0, 0);
    public static final Vector3f Y = new Vector3f(0, 1, 0);
    public static final Vector3f Z = new Vector3f(0, 0, 1);

    public void update() {
        this.model.inactive().identity().translate(position.x, position.y, position.z).scale(scale).rotate((float) Math.toRadians(rotation.y), Y).rotate((float) Math.toRadians(rotation.x), X).rotate((float) Math.toRadians(rotation.z), Z);
        this.model.swap();
    }

    public void unbind() {
        mesh.unlink();
        material.unlink();
    }

}