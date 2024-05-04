package property;

import game.Game;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

public interface Worker extends Runnable {

    long RATE = 100;
    List<Thread> THREADS = new ArrayList<>();

    @Override
    default void run() {
        final long interval = 1000000000L / RATE;
        long lastTime = System.nanoTime();
        while (!glfwWindowShouldClose(Game.window)) {
            long now = System.nanoTime();
            if (now - lastTime >= interval) {
                work();
                lastTime += interval;
            }
            while (System.nanoTime() - lastTime < interval) {
                Thread.yield();
            }
        }
    }

    void work();

    default void start() {
        Thread thread = new Thread(this);
        THREADS.add(thread);
        thread.start();
    }

    static void clear() {
        THREADS.forEach(Thread::interrupt);
    }

}
