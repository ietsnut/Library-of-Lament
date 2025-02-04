package resource;

import game.Scene;
import property.Terrain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        System.out.println("Loading: " + this);
        this.load();
        this.buffer();
        this.unload();
        System.gc();
        LOADED.add(this);
    }

    default void queue() {
        Thread thread = new Thread(this);
        try {
            thread.start();
        } finally {
            THREADS.add(thread);
        }
    }

    static void process() {
        Resource loaded;
        while ((loaded = LOADED.poll()) != null) {
            loaded.bind();
            BINDED.add(loaded);
            System.gc();
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
