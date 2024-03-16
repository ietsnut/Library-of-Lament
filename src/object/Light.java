package object;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class Light {


    // TODO: REPLACE WITH SCENE SPECIFIC LIGHTING ARRAY
    public static final ArrayList<Light> ALL = new ArrayList<>();

    public Vector3f position;
    public Vector3f attenuation;
    public float intensity;

    public Light(Vector3f position, Vector3f attenuation, float intensity) {
        this.position = position;
        this.attenuation = attenuation;
        this.intensity = intensity;
        ALL.add(this);
    }

}