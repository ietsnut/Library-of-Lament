package resource;

import engine.Console;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Resource extends Runnable {

    ConcurrentLinkedQueue<Resource> LOADED      = new ConcurrentLinkedQueue<>();
    List<Resource>                  BINDED      = new ArrayList<>();
    ExecutorService                 THREADS     = Executors.newVirtualThreadPerTaskExecutor();

    void load();
    void unload();

    void bind();
    void unbind();

    void buffer();

    boolean loaded();
    boolean binded();

    default void run() {
        Console.log("Loading", this.toString());
        this.load();
        this.buffer();
        this.unload();
        LOADED.add(this);
    }

    default Resource direct() {
        this.load();
        this.buffer();
        this.unload();
        this.bind();
        BINDED.add(this);
        return this;
    }

    default void queue() {
        THREADS.submit(this);
    }

    static void process() {
        Resource loaded;
        while ((loaded = LOADED.poll()) != null) {
            Console.log("Binding", loaded.toString());
            loaded.bind();
            BINDED.add(loaded);
            System.gc();
        }
    }

    static void clear() {
        THREADS.shutdown();
        LOADED.clear();
        for (Resource resource : BINDED) {
            Console.log("Unbinding", resource.toString());
            resource.unbind();
        }
        BINDED.forEach(Resource::unbind);
        BINDED.clear();
    }

}
