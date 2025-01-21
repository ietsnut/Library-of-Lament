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
        File[] files = new File("resource" + File.separator + type).listFiles();
        assert files != null;

        int materialsCount  = 0;
        int meshesCount     = 0;
        int musicsCount     = 0;

        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
                switch (ext) {
                    case "png":
                        materialsCount++;
                        break;
                    case "obj":
                        meshesCount++;
                        break;
                    case "mp3":
                        musicsCount++;
                        break;
                }
            }
        }

        this.materials  = new Material[materialsCount];
        this.meshes     = new Mesh[meshesCount];
        this.musics     = new Music[musicsCount];

        materialsCount = 0;
        meshesCount = 0;
        musicsCount = 0;

        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
                switch (ext) {
                    case "png":
                        this.materials[materialsCount++] = new Material(type, fileName.substring(0, fileName.lastIndexOf('.')));
                        break;
                    case "obj":
                        this.meshes[meshesCount++] = new Mesh(type, fileName.substring(0, fileName.lastIndexOf('.')));
                        break;
                    case "mp3":
                        //this.musics[musicsCount++] = new Music(type, fileName.substring(0, fileName.lastIndexOf('.')));
                        break;
                }
            }
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
