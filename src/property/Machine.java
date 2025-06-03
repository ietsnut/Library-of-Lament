package property;

import java.util.*;
import java.util.concurrent.*;

public interface Machine {

    /*
    long previousTime = System.nanoTime();
    long deltaTime = 0;
    final long timePerFrame = 1000000000 / FPS;

    @Override
    public void run() {

        long currentTime = System.nanoTime();
        deltaTime = currentTime - previousTime;
        previousTime = currentTime;
        while (deltaTime > timePerFrame) {
            deltaTime -= timePerFrame;
            update();
        }
        canvas2D.repaint();
        SwingUtilities.invokeLater(this);
    }
     */

    List<ScheduledExecutorService> MACHINES = new ArrayList<>();

    void turn();

    default void start(long interval) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MACHINES.add(scheduler);
        scheduler.scheduleAtFixedRate(this::turn, 0, 1000L / interval, TimeUnit.MILLISECONDS);
    }

    default void start() {
        start(8);
    }

    static void clear() {
        MACHINES.forEach(scheduler -> {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
        MACHINES.clear();
    }

}
