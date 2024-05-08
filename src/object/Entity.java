package object;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import property.*;
import property.Vector;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public abstract class Entity {

    public static final float UNIT  = 1f;
    public static final float DECI  = 1f / 2;
    public static final float CENTI = 1f / 4;
    public static final float MILLI = 1f / 8;
    public static final float MICRO = 1f / 16;
    public static final float NANO  = 1f / 32;
    public static final float PICO  = 1f / 64;
    public static final float FEMTO = 1f / 128;
    public static final float ATTO  = 1f / 256;

    public final Vector position    = new Vector();
    public final Vector rotation    = new Vector();
    public float scale              = UNIT;

    public final Matrix4f model     = new Matrix4f();

    public final List<Material> materials = new ArrayList<>();
    public final List<Mesh>     meshes    = new ArrayList<>();

    public final byte id;
    public final String type;

    public byte mesh        = 0;
    public byte material    = 0;

    public boolean collidable = this instanceof Interactive;

    public Entity() {
        this.type   = this.getClass().getSimpleName().toLowerCase();
        this.id     = -1;
    }

    public Entity(byte id) {
        this.type   = this.getClass().getSimpleName().toLowerCase();
        this.id     = id;
        allocate(true, true);
    }

    public Entity(byte id, Mesh mesh) {
        this.type   = this.getClass().getSimpleName().toLowerCase();
        this.id     = id;
        this.meshes.add(mesh);
        allocate(false, true);
    }

    public Entity(byte id, String type) {
        this.type   = type;
        this.id     = id;
        allocate(true, true);
    }

    public void allocate(boolean mesh, boolean material) {
        String directoryPath = "resource/" + type;
        File directory = new File(directoryPath);
        FilenameFilter filter = (dir, name) -> name.startsWith(String.valueOf(id));
        File[] files = directory.listFiles(filter);
        if (files != null && files.length > 0) {
            for (File file : files) {
                String fileName = file.getName();
                String[] parts = fileName.split("_");
                if (parts.length > 1) {
                    String state = parts[1].split("\\.")[0];
                    String fileExtension = parts[1].split("\\.")[1];
                    if (fileExtension.equals("png") && material) {
                        materials.add(new Material(id, type, state));
                    } else if (fileExtension.equals("obj") && mesh) {
                        meshes.add(new Mesh(id, type, state));
                    }
                } else {
                    String fileExtension = parts[0].split("\\.")[1];
                    if (fileExtension.equals("png") && material) {
                        materials.add(new Material(id, type));
                    } else if (fileExtension.equals("obj") && mesh) {
                        meshes.add(new Mesh(id, type));
                    }
                }
            }
        } else {
            System.out.println("No files found with the prefix " + id);
        }
    }

    public abstract void update();

    @Override
    public String toString() {
        return "<" + this.getClass().getTypeName() + "> [" + type + " : " + id + "] : " + position.x + ", " + position.y + ", " + position.z;
    }

    private Matrix4f model() {
        Matrix4f model = new Matrix4f();

        model.translate(position.x, position.y, position.z);
        model.scale(scale);
        model.rotate((float) Math.toRadians(rotation.y * 5), new Vector3f(0, 1, 0));
        model.rotate((float) Math.toRadians(rotation.x * 5), new Vector3f(1, 0, 0));
        model.rotate((float) Math.toRadians(rotation.z * 5), new Vector3f(0, 0, 1));

        return model;
    }

    public void remodel() {
        this.model.set(model());
    }


}
