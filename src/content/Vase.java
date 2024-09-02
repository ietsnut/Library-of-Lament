package content;

import component.Light;
import org.joml.Vector3f;
import property.Entity;
import component.Machine;
import resource.Material;
import resource.Mesh;

public class Vase extends Entity implements Machine {

    long shake;

    public Vase(String name) {
        super(1);
        start();
        shake = System.currentTimeMillis();
    }

    @Override
    public void turn() {
        this.rotation.y += 1;
        if (System.currentTimeMillis() - shake > 1000) {
            shake = System.currentTimeMillis();
            this.material = this.material == 0 ? 1 : 0;
        }
        remodel();
    }

}
