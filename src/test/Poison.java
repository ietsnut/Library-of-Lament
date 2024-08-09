package test;

public class Poison extends Unit {

    public Poison() {
        super(2, 1);
    }

    @Override
    public Unit compare(Unit unit) {
        return unit;
    }

    @Override
    public void self() {

    }

}
