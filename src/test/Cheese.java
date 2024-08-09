package test;

public class Cheese extends Unit {

    public Cheese() {
        super(1, 1);
        this.attributes[0] = 5;
    }

    @Override
    public Unit compare(Unit unit) {
        return unit;
    }

    @Override
    public void self() {

    }

}
