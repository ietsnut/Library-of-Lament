package property;

public abstract class Animation {

    Transformation origin;
    Transformation target;

    public Animation(Transformation origin, Transformation target) {
        this.origin = origin;
        this.target = target;
    }

    public abstract void animate();

    enum Type {

    }

}
