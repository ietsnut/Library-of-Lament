package resource;

import component.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface Resource extends Runnable, Component {

    ConcurrentLinkedQueue<Resource>   LOADED      = new ConcurrentLinkedQueue<>();
    List<Resource>                    BINDED      = new ArrayList<>();
    List<Thread>                      THREADS     = new ArrayList<>();

    void load();
    void unload();

    void bind();
    void unbind();

    void buffer();

    boolean loaded();
    boolean binded();

    default void direct() {
        this.load();
        this.buffer();
        this.bind();
        this.unload();
        BINDED.add(this);
    }

    default void run() {
        this.load();
        this.buffer();
        LOADED.add(this);
    }

    default void queue() {
        Thread thread = Thread.ofVirtual().start(this);
        THREADS.add(thread);
    }

    static Resource process() {
        Resource loaded = LOADED.poll();
        if (loaded != null) {
            loaded.bind();
            loaded.unload();
            BINDED.add(loaded);
            java.lang.System.gc();
        }
        return loaded;
    }

    static void clear() {
        THREADS.forEach(Thread::interrupt);
        THREADS.clear();
        LOADED.clear();
        BINDED.forEach(Resource::unbind);
        BINDED.clear();
    }

}
