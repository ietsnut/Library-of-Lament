package property;

public interface Interaction extends Intention {

    default void intent() {
        interact();
    }

    public void interact();

}
