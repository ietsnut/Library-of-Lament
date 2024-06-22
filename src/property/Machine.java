package property;

import java.util.*;
import java.util.concurrent.*;

public interface Machine {

    List<ScheduledExecutorService> MACHINES = new ArrayList<>();

    void process();

    default void start(long interval) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MACHINES.add(scheduler);
        scheduler.scheduleAtFixedRate(this::process, 0, 1000L / interval, TimeUnit.MILLISECONDS);
    }

    default void start() {
        start(8);
    }

    static void stop() {
        MACHINES.forEach(ScheduledExecutorService::shutdownNow);
    }

}
