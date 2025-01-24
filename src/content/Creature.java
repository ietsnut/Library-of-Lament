package content;

import property.Entity;
import resource.Material;
import resource.Mesh;

public class Creature extends Entity {

    public Creature() {
        super(new Material("terrain", "forest"), Mesh.PLANE);
    }



}
