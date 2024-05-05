package property;

import object.Load;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Watcher implements Worker {

    private final WatchService service;

    public Watcher() {
        try {
            service = FileSystems.getDefault().newWatchService();
            Path path = Paths.get("resource");
            path.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void listen() {
        Watcher watcher = new Watcher();
        watcher.start();
    }

    @Override
    public void work() {
        try {
            WatchKey key;
            while ((key = service.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path type = (Path) event.context();
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Load.queue(type.getFileName().toString());
                        //new Thread(() -> Load.queue(type.getFileName().toString())).start();
                        //Load.QUEUE.add(type.getFileName().toString());
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            start();
        }
    }

}
