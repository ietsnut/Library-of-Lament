package property;

import resource.Material;
import resource.Mesh;
import resource.Music;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;

public class Entity extends State {

    public final String type = this.getClass().getSimpleName().toLowerCase();

    public final Vector3f position  = new Vector3f(0);
    public final Vector3f rotation  = new Vector3f(0);
    public final Vector3f scale     = new Vector3f(1);
    public final Matrix4f model     = new Matrix4f();

    public final Material[] materials;
    public final Mesh[]     meshes;
    public final Music[]    musics;

    public int material = 0;
    public int mesh     = 0;
    public int music    = 0;

    public Entity(int states) {
        super(states);
        this.materials  = new Material[states];
        this.meshes     = new Mesh[states];
        this.musics     = new Music[states];
        for (int state = 0; state < states; state++) {
            this.materials[state]   = new Material(type, state);
            this.meshes[state]      = new Mesh(type, state);
            //this.musics[musicsCount++] = new Music(type, fileName.substring(0, fileName.lastIndexOf('.')));
        }
    }

    @Override
    public String toString() {
        return "< " + type + " > [ state: " + state() + " ] : " + position.x + ", " + position.y + ", " + position.z + " : " + rotation.x + ", " + rotation.y + ", " + rotation.z + " : " + scale;
    }

    private Matrix4f model() {
        return model.identity().translate(position.x, position.y, position.z).scale(scale).rotate((float) Math.toRadians(rotation.y * 5), new Vector3f(0, 1, 0)).rotate((float) Math.toRadians(rotation.x * 5), new Vector3f(1, 0, 0)).rotate((float) Math.toRadians(rotation.z * 5), new Vector3f(0, 0, 1));
    }

    public void remodel() {
        this.model.set(model());
    }

}
