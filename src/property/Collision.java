package property;

public sealed interface Collision extends Intention permits Entity {

    default void intent() {
        collide();
    }

    void collide();

}
