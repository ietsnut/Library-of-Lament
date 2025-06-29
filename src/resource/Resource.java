package resource;

import engine.Console;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Resource extends Runnable, Serializable {

    ConcurrentLinkedQueue<Resource> LOADED = new ConcurrentLinkedQueue<>();
    ExecutorService                 LOADER = Executors.newVirtualThreadPerTaskExecutor();
    List<Resource>                  LINKED = new ArrayList<>();

    void load();
    void buffer();
    void unload();

    void link();
    void unlink();

    void bind();
    void unbind();

    boolean linked();

    default void run() {
        try {
            Console.log("Loading", this.toString());
            this.load();
            this.buffer();
            this.unload();
            LOADED.add(this);
        } catch (Exception e) {
            Console.error(e);
        }
    }

    default Resource direct() {
        try {
            this.load();
            this.buffer();
            this.unload();
            this.link();
            LINKED.add(this);
        } catch (Exception e) {
            Console.error(e);
        }
        return this;
    }

    default Resource queue() {
        LOADER.submit(this);
        return this;
    }

    static void process() {
        Resource loaded;
        while ((loaded = LOADED.poll()) != null) {
            try {
                Console.log("Linking", loaded.toString());
                loaded.link();
                LINKED.add(loaded);
                System.gc();
            } catch (Exception e) {
                Console.error(e);
            }
        }
    }

    static void clear() {
        LOADER.shutdown();
        LOADED.clear();
        LINKED.forEach(Resource::unlink);
        LINKED.clear();
    }

}
