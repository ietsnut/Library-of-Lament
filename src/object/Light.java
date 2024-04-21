package object;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class Light {

    public Vector3f     position;
    public Vector3f     attenuation;
    public float        intensity;

    public Light(Vector3f position, Vector3f attenuation, float intensity) {
        this.position       = position;
        this.attenuation    = attenuation;
        this.intensity      = intensity;
    }

}