package object;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class Light {

    public int ID;

    public static final ArrayList<Light> ALL = new ArrayList<>();

    public Vector3f position;
    public Vector3f intensity;

    public Light(Vector3f position, Vector3f intensity) {
        this.position = position;
        this.intensity = intensity;
        this.ID = ALL.size();
        ALL.add(this);
    }

    public Light(Vector3f position) {
        this.position = position;
        this.intensity = new Vector3f(1, 0, 0);
        this.ID = ALL.size();
        ALL.add(this);
    }

}