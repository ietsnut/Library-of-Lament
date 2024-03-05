package network;

import java.io.Serial;
import java.io.Serializable;

public class State implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    public int id;
    public int x;
    public int y;

}
