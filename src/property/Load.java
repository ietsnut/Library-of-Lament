package property;

import object.Entity;

import java.util.*;
import java.util.concurrent.*;

public interface Load extends Runnable {

    // loads get instantiated on the main thread, and then loaded on a loader thread

    ConcurrentLinkedQueue<Load> QUEUE = new ConcurrentLinkedQueue<>();

    List<Load> BOUND = new ArrayList<>();
    List<Thread> THREADS = new ArrayList<>();

    default void queue() {
        this.preload();
        Thread thread = new Thread(this);
        THREADS.add(thread);
        thread.start();
    }

    default void direct() {
        this.preload();
        this.load();
        this.postload();
        this.bind();
        BOUND.add(this);
    }

    void preload();     // thread
    void load();        // thread
    void postload();    // main

    void bind();        // main
    void unbind();      // main

    boolean reload();

    default boolean bound() {
        return BOUND.contains(this);
    }

    @Override
    default void run() {
        this.load();
        this.postload();
        QUEUE.add(this);
    }

    static void process() {
        Load load = QUEUE.poll();
        if (load != null) {
            load.bind();
            BOUND.add(load);
        }
        Iterator<Load> loads = Load.BOUND.iterator();
        while (loads.hasNext()) {
            Load bound = loads.next();
            if (bound.reload()) {
                loads.remove();
                bound.unbind();
                bound.queue();
            }
        }
    }

    static void clear() {
        THREADS.forEach(Thread::interrupt);
        QUEUE.clear();
        for (Load load : BOUND) {
            load.unbind();
        }
        BOUND.clear();
    }

}
