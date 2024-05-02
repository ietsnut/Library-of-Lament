package object;

public class Player {

    public byte id;
    public byte x, y, z;

    public Player(byte id, byte x, byte y, byte z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float encode() {
        return (id << 24) | (x << 16) | (y << 8) | z;
    }

    public void decode(float f) {
        int b   = Float.floatToIntBits(f);
        id      = (byte) (b >> 24 & 0xFF);
        x       = (byte) (b >> 16 & 0xFF);
        y       = (byte) (b >> 8 & 0xFF);
        z       = (byte) (b & 0xFF);
    }

}
