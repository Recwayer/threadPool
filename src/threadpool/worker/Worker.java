package threadpool.worker;

import threadpool.impl.CustomThreadPool;

import java.util.concurrent.atomic.AtomicInteger;


public class Worker implements Runnable {

    private final TaskQueue queue;
    private final CustomThreadPool pool;
    private final String name;
    private final AtomicInteger activeWorkers;

    public Worker(TaskQueue queue, CustomThreadPool pool, String name, AtomicInteger activeWorkers) {
        this.queue = queue;
        this.pool = pool;
        this.name = name;
        this.activeWorkers = activeWorkers;
    }

    @Override
    public void run() {
        try {
            while (!pool.isShutdown() || !queueIsEmptyAndShutdown()) {
                Runnable task = queue.poll(pool.getKeepAliveTime(), pool.getTimeUnit());
                if (task == null) {
                    if (pool.canWorkerTerminate()) {
                        System.out.println("[Worker] " + name + " idle timeout, stopping.");
                        break;
                    }
                    continue;
                }
                executeTask(task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("[Worker] " + name + " terminated.");
            pool.workerTerminated(Thread.currentThread());
        }
    }

    private boolean queueIsEmptyAndShutdown() {
        return pool.isShutdown() && queue.size() == 0;
    }

    private void executeTask(Runnable task) {
        System.out.println("[Worker] " + name + " executes " + task);
        activeWorkers.incrementAndGet();
        try {
            task.run();
        } catch (Exception e) {
            System.err.println("[Worker] Error executing task in " + name + ": " + e);
        } finally {
            activeWorkers.decrementAndGet();
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}