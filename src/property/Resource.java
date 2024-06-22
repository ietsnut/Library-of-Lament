package property;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Resource implements Runnable {

    public static final ConcurrentLinkedQueue<Resource>     LOADED      = new ConcurrentLinkedQueue<>();
    public static final List<Resource>                      RESOURCES   = new ArrayList<>();
    public static final List<Thread>                        THREADS     = new ArrayList<>();

    public final String extension = this.getClass().getSimpleName().toLowerCase();
    public final String type, state;
    public final int id;

    public boolean loaded;
    public boolean bound;

    public Resource() {
        this.id     = -1;
        this.type   = null;
        this.state  = null;
    }

    public Resource(int id, String type) {
        this.id     = id;
        this.type   = type;
        this.state  = null;
        queue();
    }

    public Resource(int id, String type, String state) {
        this.id = id;
        this.type = type;
        this.state = state;
        queue();
    }

    public abstract void load();
    public abstract void buffer();

    public abstract void bind();
    public abstract void prepare();

    public abstract void unbind();

    @Override
    public String toString() {
        return "<" + extension + ">  [" + type + " : " + id + " : " + state + " ] = " + loaded + ", " + bound;
    }

    @Override
    public void run() {
        this.load();
        this.buffer();
        LOADED.add(this);
        loaded = true;
    }

    public void queue() {
        Thread thread = new Thread(this);
        THREADS.add(thread);
        thread.start();
    }

    public static void process() {
        Resource loaded = LOADED.poll();
        if (loaded != null) {
            loaded.bind();
            loaded.prepare();
            RESOURCES.add(loaded);
            loaded.bound = true;
            System.gc();
        }
    }

    public static void clear() {
        THREADS.forEach(Thread::interrupt);
        LOADED.clear();
        RESOURCES.forEach(Resource::unbind);
        RESOURCES.clear();
    }

}
