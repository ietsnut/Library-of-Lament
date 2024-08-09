package test;

import java.util.Arrays;
import java.util.Random;

public abstract class Unit {

    final byte hash;
    int instance;
    byte[] attributes;

    byte x = 0;
    byte y = 0;
    byte z = 0;

    public Unit(int hash, int attributes) {
        this.hash = (byte) hash;
        this.attributes = new byte[attributes];
        this.instance = hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "hash=" + hash +
                ", instance=" + instance +
                ", attributes=" + Arrays.toString(attributes) +
                ", x=" + x +
                ", y=" + y +
                ", y=" + y +
                '}';
    }

    public abstract Unit compare(Unit unit);
    public abstract void self();

    private void repel(int i1, int i2) {
        attributes[i1] += (byte) ((attributes[i1] < attributes[i2]) ? -1 : 1);
    }

    private void attract(int i1, int i2) {
        attributes[i1] += (byte) ((attributes[i1] > attributes[i2]) ? -1 : 1);
    }



}
