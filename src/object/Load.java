package object;

import property.Worker;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public interface Load extends Runnable {

    ConcurrentLinkedQueue<Load> QUEUE = new ConcurrentLinkedQueue<>();

    List<Load>              LOADED  = new ArrayList<>();
    List<Thread>            THREADS = new ArrayList<>();

    //Map<Resource, Load>   LOADERS = new HashMap<>();
    //Map<String, Watch>    WATCHES = new HashMap<>();

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

    default void direct() {
        this.load();
        this.prepare();
        this.bind();
        this.clean();
        LOADED.add(this);
    }

    void load();
    void prepare();
    void bind();
    void clean();

    void unbind();

    default boolean loaded() {
        return LOADED.contains(this);
    }

    @Override
    default void run() {
        this.load();
        this.prepare();
        QUEUE.add(this);
    }

    static void process() {
        Load load = QUEUE.poll();
        if (load != null) {
            if (load.loaded()) {
                LOADED.remove(load);
                load.unbind();
                load.queue();
            } else {
                load.bind();
                load.clean();
                LOADED.add(load);
                /*
                if (!WATCHES.containsKey(load.getClass().getSimpleName())) {
                    WATCHES.put(resourcee.type, new Watch(resourcee.type));
                }
                 */
            }
        }
    }

    static void clear() {
        THREADS.forEach(Thread::interrupt);
        QUEUE.clear();
        for (Load load : LOADED) {
            load.unbind();
        }
        LOADED.clear();
    }

    class Watch implements Worker {
        private final Path path;
        private final WatchService service;
        public Watch(String type) {
            this.path = Paths.get("resource" + File.separator + type);
            try {
                service = FileSystems.getDefault().newWatchService();
                path.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            start();
        }
        @Override
        public void work() {
            try {
                WatchKey key;
                while ((key = service.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path type = (Path) event.context();
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            System.out.println("Changed resource: " + type.getFileName());
                            //Resource.queue(type.getFileName().toString());
                        }
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                start();
            }
        }
    }

}
