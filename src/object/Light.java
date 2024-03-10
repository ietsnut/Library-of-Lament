package object;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class Light {


    // TODO: REPLACE WITH SCENE SPECIFIC LIGHTING ARRAY
    public static final ArrayList<Light> ALL = new ArrayList<>();

    public Vector3f position;
    public Vector3f attenuation;

    public Light(Vector3f position, Vector3f attenuation) {
        this.position = position;
        this.attenuation = attenuation;
        ALL.add(this);
    }

}