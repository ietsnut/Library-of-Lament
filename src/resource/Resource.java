package resource;

import property.Terrain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public interface Resource extends Runnable {

    Map<Class<? extends Resource>, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    ConcurrentLinkedQueue<Resource>   LOADED      = new ConcurrentLinkedQueue<>();
    List<Resource>                    BINDED      = new ArrayList<>();
    List<Thread>                      THREADS     = new ArrayList<>();

    void load();
    void unload();

    void bind();
    void unbind();

    void buffer();

    boolean loaded();
    boolean binded();

    default ReentrantLock getLock() {
        Class<? extends Resource> resourceClass = this.getClass();
        while (resourceClass != null) {
            ReentrantLock lock = LOCKS.get(resourceClass);
            if (lock != null) {
                return lock;
            }
            resourceClass = (Class<? extends Resource>) resourceClass.getSuperclass();
        }
        return LOCKS.computeIfAbsent(this.getClass(), k -> new ReentrantLock());
    }

    default void run() {
        ReentrantLock lock = getLock();
        lock.lock();
        try {
            this.load();
            this.buffer();
            this.unload();
            LOADED.add(this);
            synchronized (this) {
                while (!binded()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    default void queue() {
        Thread thread = new Thread(this);
        try {
            thread.start();
        } finally {
            THREADS.add(thread);
        }
    }

    static void process() {
        Resource loaded;
        while ((loaded = LOADED.poll()) != null) {
            loaded.bind();
            BINDED.add(loaded);
            synchronized (loaded) {
                loaded.notifyAll();
            }
            System.gc();
        }
    }

    static void clear() {
        THREADS.forEach(Thread::interrupt);
        THREADS.clear();
        LOADED.clear();
        BINDED.forEach(Resource::unbind);
        BINDED.clear();
    }

}
