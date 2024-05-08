package property;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Resource implements Runnable {

    public static final ConcurrentLinkedQueue<Resource>     QUEUE       = new ConcurrentLinkedQueue<>();
    public static final List<Resource>                      RESOURCES   = new ArrayList<>();
    public static final Map<String, Watch>                  WATCHES     = new HashMap<>();
    public static final List<Thread>                        THREADS     = new ArrayList<>();

    // extension: mesh, material, texture, shader, sound, music, font
    public final String extension = this.getClass().getSimpleName().toLowerCase();
    // type: object, entity, scene, level, world, game
    public final String type, state;
    // id: name of the resource
    // state: state of the resource
    public final byte id;

    public boolean loaded;
    public boolean bound;

    public Resource() {
        this.id     = -1;
        this.type   = null;
        this.state  = null;
    }

    public Resource(byte id, String type) {
        this.id     = id;
        this.type   = type;
        this.state  = null;
        queue();
    }

    public Resource(byte id, String type, String state) {
        this.id     = id;
        this.type   = type;
        this.state  = state;
        queue();
    }

    public abstract void load();
    public abstract void postload();

    public abstract void bind();
    public abstract void postbind();

    public abstract void unbind();

    @Override
    public String toString() {
        return "<" + extension + ">  [" + type + " : " + id + " : " + state + " ] = " + loaded + ", " + bound;
    }

    @Override
    public void run() {
        this.load();
        this.postload();
        QUEUE.add(this);
        this.loaded = true;
    }

    public void queue() {
        Thread thread = new Thread(this);
        THREADS.add(thread);
        thread.start();
    }

    public static void queue(String type, String name, String state, String extension) {
        System.out.println("Wanna queue: " + type + " : " + name);
        new Thread(() -> {
            for (Resource resource : RESOURCES) {
                // resource.queue;
            }
        }).start();
    }

    public void direct() {
        this.load();
        this.postload();
        this.bind();
        this.postbind();
        RESOURCES.add(this);
    }

    public static void process() {
        Resource resource = QUEUE.poll();
        if (resource != null) {
            if (resource.bound) {
                resource.bound  = false;
                resource.loaded = false;
                RESOURCES.remove(resource);
                resource.unbind();
                resource.queue();
            } else {
                resource.bind();
                resource.postbind();
                RESOURCES.add(resource);
                resource.bound = true;
                if (resource.type != null && !WATCHES.containsKey(resource.type)) {
                    WATCHES.put(resource.type, new Watch(resource.type));
                }
            }
        }
    }

    public static void clear() {
        THREADS.forEach(Thread::interrupt);
        QUEUE.clear();
        RESOURCES.forEach(Resource::unbind);
        RESOURCES.clear();
    }

    private static class Watch implements Worker {
        private final WatchService service;
        private final String type;
        public Watch(String type) {
            this.type = type;
            Path path = Paths.get("resource" + File.separator + type);
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
                            System.out.println(type + " : " + type.getFileName());
                            String fileName = type.getFileName().toFile().getName();
                            String[] parts = fileName.split("_");
                            System.out.println(Arrays.toString(parts));
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
