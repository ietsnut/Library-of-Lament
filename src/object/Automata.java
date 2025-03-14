package object;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import resource.Material;
import resource.Mesh;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

public abstract class Automata {

    public final String type = this.getClass().getSimpleName().toLowerCase();

    public final Vector3f scale = new Vector3f(1);
    public final Quaternionf orientation = new Quaternionf();

    public final Matrix4f model     = new Matrix4f();
    public final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    public final Mesh       mesh;
    public final Material   material;

    public final Component[] components;

    public Automata(String... components) {
        this.mesh     = new Mesh(type);
        this.material = new Material(type);
        this.components = new Component[components.length];
        for (int i = 0; i < components.length; i++) {
            this.components[i] = new Component(components[i]);
        }
    }

    public void update() {
        model.identity()
                .rotate(orientation)
                .scale(scale)
                .get(buffer);
        for (Component component : this.components) {
            update(component);
            component.model
                    .set(model)
                    .translate(component.position)
                    .rotate(component.orientation)
                    .scale(component.scale)
                    .get(component.buffer);
        }
    }

    abstract public void update(Component component);

    public class Component {

        public final Vector3f position  = new Vector3f(0);
        public final Vector3f scale = new Vector3f(1);
        public final Quaternionf orientation = new Quaternionf();

        public final Matrix4f model     = new Matrix4f();
        public final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

        public final Mesh mesh;
        public final String id;

        public Component(String id) {
            this.id = id;
            this.mesh = new Mesh(Automata.this.type + "_" + id);
        }

    }

}
