package resource;

import property.Terrain;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public interface Resource extends Runnable {

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

    default void run() {
        this.load();
        LOADED.add(this);
    }

    default void queue() {
        Thread thread = Thread.ofVirtual().start(this);
        THREADS.add(thread);
    }

    static void process() {
        Resource loaded;
        while ((loaded = LOADED.poll()) != null) {
            loaded.buffer();
            loaded.bind();
            BINDED.add(loaded);
            loaded.unload();
            java.lang.System.gc();
        }
    }

    static void clear() {
        THREADS.forEach(Thread::interrupt);
        THREADS.clear();
        LOADED.clear();
        BINDED.forEach(Resource::unbind);
        BINDED.clear();
    }

}
