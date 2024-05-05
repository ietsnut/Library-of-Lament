package object;

import java.util.*;
import java.util.concurrent.*;

public sealed interface Load extends Runnable permits Entity, Texture {

    ConcurrentLinkedQueue<Load> QUEUE = new ConcurrentLinkedQueue<>();

    List<Load>      BOUND   = new ArrayList<>();
    List<Thread>    THREADS = new ArrayList<>();

    default void queue() {
        Thread thread = new Thread(this);
        THREADS.add(thread);
        thread.start();
    }

    static void queue(Load load) {
        Thread thread = new Thread(load);
        THREADS.add(thread);
        thread.start();
    }

    static void queue(String type) {
        new Thread(() -> {
            Iterator<Load> loads = Load.BOUND.iterator();
            while (loads.hasNext()) {
                Load bound = loads.next();
                if (bound.getClass().getSimpleName().toLowerCase().equals(type) || (bound instanceof Texture texture && texture.type != null && texture.type.equals(type))) {
                    bound.queue();
                    System.out.println("Queued: " + bound.getClass().getSimpleName());
                }
            }
        }).start();
    }

    default void direct() {
        this.load();
        this.postload();
        this.bind();
        BOUND.add(this);
    }

    void load();
    void postload();

    void bind();
    void unbind();

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
            if (load.bound()) {
                BOUND.remove(load);
                load.unbind();
                load.queue();
            } else {
                load.bind();
                BOUND.add(load);
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
