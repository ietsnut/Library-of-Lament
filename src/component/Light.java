package component;

import org.joml.Vector3f;

public class Light implements Component {

    public Vector3f     attenuation;
    public float        intensity;

    public Light(Vector3f attenuation, float intensity) {
        this.attenuation    = attenuation;
        this.intensity      = intensity;
    }

}