package property;

import object.Matrix;
import resource.Material;
import resource.Mesh;
import org.joml.Vector3f;

import java.util.ArrayList;
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

    int states;
    public int state = 0;

    private static final Map<String, Material> materialCache = new HashMap<>();
    private static final Map<String, Mesh> meshCache = new HashMap<>();

    public Entity(Material material, Mesh mesh) {
        this.states     = 1;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];
        this.materials[state] = material;
        this.meshes[state]    = mesh;
    }

    // 2. When a Mesh is provided along with a name, create (or reuse) the Material.
    public Entity(Mesh mesh, String name) {
        this.states     = 1;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];

        String materialKey = type + ":" + name;
        Material cachedMaterial = materialCache.get(materialKey);
        if (cachedMaterial == null) {
            cachedMaterial = new Material(type, name);
            materialCache.put(materialKey, cachedMaterial);
        }
        this.materials[state] = cachedMaterial;
        this.meshes[state]    = mesh;
    }

    // 3. When only a name is provided, create (or reuse) both Material and Mesh.
    public Entity(String name) {
        this.states     = 1;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];

        String materialKey = type + ":" + name;
        Material cachedMaterial = materialCache.get(materialKey);
        if (cachedMaterial == null) {
            cachedMaterial = new Material(type, name);
            materialCache.put(materialKey, cachedMaterial);
        }
        this.materials[state] = cachedMaterial;

        String meshKey = type + ":" + name;
        Mesh cachedMesh = meshCache.get(meshKey);
        if (cachedMesh == null) {
            cachedMesh = new Mesh(type, name);
            meshCache.put(meshKey, cachedMesh);
        }
        this.meshes[state] = cachedMesh;
    }

    // 4. When multiple states are used with a provided Mesh,
    //    create (or reuse) a Material for each state.
    public Entity(int states, Mesh mesh) {
        this.states     = states;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];
        for (int i = 0; i < states; i++) {
            String key = type + ":" + i;
            Material cachedMaterial = materialCache.get(key);
            if (cachedMaterial == null) {
                cachedMaterial = new Material(type, i);
                materialCache.put(key, cachedMaterial);
            }
            this.materials[i] = cachedMaterial;
            this.meshes[i]    = mesh; // Mesh provided externally; assumed already managed.
        }
    }

    // 5. When multiple states are used and both Material and Mesh need to be created,
    //    create (or reuse) each one.
    public Entity(int states) {
        this.states     = states;
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];
        for (int i = 0; i < states; i++) {
            String key = type + ":" + i;
            Material cachedMaterial = materialCache.get(key);
            if (cachedMaterial == null) {
                cachedMaterial = new Material(type, i);
                materialCache.put(key, cachedMaterial);
            }
            this.materials[i] = cachedMaterial;

            Mesh cachedMesh = meshCache.get(key);
            if (cachedMesh == null) {
                cachedMesh = new Mesh(type, i);
                meshCache.put(key, cachedMesh);
            }
            this.meshes[i] = cachedMesh;
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
