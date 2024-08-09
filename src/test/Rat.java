package test;

public class Rat extends Unit {

    public Rat() {
        super(0, 5);
    }

    @Override
    public Unit compare(Unit unit) {
        if (unit.hash == 1) {
            this.attributes[3] += unit.attributes[0];
        }
        if (unit.hash == 2) {
            this.attributes[3] -= unit.attributes[0];
        }
        return unit;
    }

    @Override
    public void self() {

    }

}
