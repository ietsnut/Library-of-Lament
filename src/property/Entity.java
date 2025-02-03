package property;

import object.Matrix;
import resource.Material;
import resource.Mesh;
import org.joml.Vector3f;

public class Entity {

    public final String type = this.getClass().getSimpleName().toLowerCase();

    public final Vector3f position  = new Vector3f(0);
    public final Vector3f rotation  = new Vector3f(0);
    public final Vector3f scale     = new Vector3f(1);

    public final Matrix   model     = new Matrix();

    public final Material[] materials;
    public final Mesh[]     meshes;

    int states;
    public int state = 0;

    //TODO: Add invalid state detection and constrain

    public Entity(Material material, Mesh mesh) {
        this.states     = 1;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];
        this.materials[state]   = material;
        this.meshes[state]      = mesh;
    }

    public Entity(Mesh mesh, String name) {
        this.states     = 1;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];
        this.materials[state]   = new Material(type, name);
        this.meshes[state]      = mesh;
    }

    public Entity(String name) {
        this.states     = 1;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];
        this.materials[state]   = new Material(type, name);
        this.meshes[state]      = new Mesh(type, name);
    }

    public Entity(int states) {
        this.states     = states;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];
        for (int state = 0; state < states; state++) {
            this.materials[state]   = new Material(type, state);
            this.meshes[state]      = new Mesh(type, state);
        }
    }

    @Override
    public String toString() {
        return "< " + type + " > [ state: " + state + " ] : " + position.x + ", " + position.y + ", " + position.z + " : " + rotation.x + ", " + rotation.y + ", " + rotation.z + " : " + scale;
    }

    public static final Vector3f X = new Vector3f(1, 0, 0);
    public static final Vector3f Y = new Vector3f(0, 1, 0);
    public static final Vector3f Z = new Vector3f(0, 0, 1);

    public void update() {
        this.model.buffer().identity().translate(position.x, position.y, position.z).scale(scale).rotate((float) Math.toRadians(rotation.y), Y).rotate((float) Math.toRadians(rotation.x), X).rotate((float) Math.toRadians(rotation.z), Z);
        this.model.swap();
    }



}
