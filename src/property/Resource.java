package property;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface Resource extends Runnable {

    ConcurrentLinkedQueue<Resource>     LOADED      = new ConcurrentLinkedQueue<>();
    List<Resource>                      BINDED      = new ArrayList<>();
    List<Thread>                        THREADS     = new ArrayList<>();

    void load();
    void unload();

    void bind();
    void unbind();

    void buffer();

    default Resource direct() {
        this.load();
        this.buffer();
        this.bind();
        BINDED.add(this);
        return this;
    }

    default void run() {
        this.load();
        this.buffer();
        LOADED.add(this);
    }

    default Resource queue() {
        Thread thread = new Thread(this);
        THREADS.add(thread);
        thread.start();
        return this;
    }

    static Resource process() {
        Resource loaded = LOADED.poll();
        if (loaded != null) {
            loaded.bind();
            BINDED.add(loaded);
            java.lang.System.gc();
        }
        return loaded;
    }

    static void clear() {
        THREADS.forEach(Thread::interrupt);
        LOADED.clear();
        BINDED.forEach(Resource::unload);
        BINDED.forEach(Resource::unbind);
        BINDED.clear();
    }

}