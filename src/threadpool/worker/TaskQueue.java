package threadpool.worker;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskQueue {
    private final int id;
    private final BlockingQueue<Runnable> queue;

    public TaskQueue(int id, int capacity) {
        this.id = id;
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public boolean offer(Runnable task) {
        boolean accepted = queue.offer(task);
        if (accepted) {
            System.out.println("[Pool] Task accepted into queue #" + id + ": " + task);
        }
        return accepted;
    }

    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public int size() {
        return queue.size();
    }
}