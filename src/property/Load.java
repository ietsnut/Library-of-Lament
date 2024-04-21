package property;

import java.util.*;
import java.util.concurrent.*;

public interface Load extends Runnable {

    ConcurrentLinkedQueue<Load> QUEUE = new ConcurrentLinkedQueue<>();
    List<Load> BOUND = new ArrayList<>();

    default void enqueue() {
        new Thread(this).start();
    }

    void load();

    void bind();

    static Load process() {
        Load load = QUEUE.poll();
        if (load != null) {
            load.bind();
            BOUND.add(load);
        }
        return load;
    }

    @Override
    default void run() {
        this.load();
        QUEUE.add(this);
    }

}
