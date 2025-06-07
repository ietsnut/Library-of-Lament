package property;

import object.Matrix;
import resource.Material;
import resource.Mesh;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class Entity {

    public final String type = this.getClass().getSimpleName().toLowerCase();

    public final Vector3f position  = new Vector3f(0);
    public final Vector3f rotation  = new Vector3f(0);
    public final Vector3f scale     = new Vector3f(1);

    public final Matrix   model     = new Matrix();

    public final Material[] materials;
    public final Mesh[]     meshes;

    final int states;
    public int state = 0;

    private static final Map<String, Mesh>      meshCache       = new HashMap<>();
    private static final Map<String, Material>  materialCache   = new HashMap<>();

    public Entity(Mesh mesh, Material material) {
        this.states = 1;
        this.meshes = new Mesh[states];
        this.materials = new Material[states];
        this.meshes[state] = mesh;
        this.materials[state] = material;
    }

    public Entity(Mesh mesh, String name) {
        this.states = 1;
        this.materials = new Material[states];
        this.meshes = new Mesh[states];
        this.materials[state] = materialCache.computeIfAbsent(type + name, k -> new Material(type, name));
        this.meshes[state] = mesh;
    }

    public Entity(String name) {
        this.states = 1;
        this.materials = new Material[states];
        this.meshes = new Mesh[states];
        this.materials[state] = materialCache.computeIfAbsent(type + name, k -> new Material(type, name));
        this.meshes[state] = meshCache.computeIfAbsent(type + name, k -> new Mesh(type, name));
    }

    @Override
    public String toString() {
        return "< " + type + " > [state: " + state + "]: " + position.x + ", " + position.y + ", " + position.z + " : " + rotation.x + ", " + rotation.y + ", " + rotation.z + " : " + scale;
    }

    public static final Vector3f X = new Vector3f(1, 0, 0);
    public static final Vector3f Y = new Vector3f(0, 1, 0);
    public static final Vector3f Z = new Vector3f(0, 0, 1);

    public void update() {
        this.model.inactive().identity().translate(position.x, position.y, position.z).scale(scale).rotate((float) Math.toRadians(rotation.y), Y).rotate((float) Math.toRadians(rotation.x), X).rotate((float) Math.toRadians(rotation.z), Z);
        this.model.swap();
    }

    public void unbind() {
        for (Mesh mesh : meshes) {
            mesh.unbind();
        }
        for (Material material : materials) {
            material.unbind();
        }
    }

}
