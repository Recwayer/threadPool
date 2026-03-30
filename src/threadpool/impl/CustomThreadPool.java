package threadpool.impl;

import threadpool.CustomExecutor;
import threadpool.config.balancer.ExecutionBalancer;
import threadpool.config.balancer.impl.RoundRobinBalancer;
import threadpool.config.policy.RejectionPolicy;
import threadpool.config.policy.impl.AbortPolicy;
import threadpool.factory.CustomThreadFactory;
import threadpool.worker.TaskQueue;
import threadpool.worker.Worker;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements CustomExecutor {

    private final AtomicInteger activeWorkers = new AtomicInteger(0);
    private final static String PREFIX_FOR_NAME_WORKER = "Pool-worker-";

    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int minSpareThreads;

    private final ThreadFactory threadFactory;
    private ExecutionBalancer balancer;
    private RejectionPolicy rejectionPolicy;

    private final List<TaskQueue> queues = new ArrayList<>();
    private final List<Thread> workers = Collections.synchronizedList(new ArrayList<>());

    private volatile boolean shutdown = false;

    public CustomThreadPool(int corePoolSize, int maxPoolSize, int minSpareThreads,
                            int queueSize, long keepAliveTime, TimeUnit timeUnit) {

        validateParams(corePoolSize, maxPoolSize, minSpareThreads, queueSize, keepAliveTime, timeUnit);

        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.minSpareThreads = Math.max(minSpareThreads, corePoolSize);
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.rejectionPolicy = new AbortPolicy();
        this.threadFactory = new CustomThreadFactory();
        for (int i = 0; i < maxPoolSize; i++) {
            queues.add(new TaskQueue(i, queueSize));
        }
        this.balancer = new RoundRobinBalancer(queues);

        for (int i = 0; i < corePoolSize; i++) {
            createWorker();
        }
    }

    private synchronized void createWorker() {
        if (workers.size() >= maxPoolSize) {
            return;
        }
        int queueIndex = workers.size();
        TaskQueue queue = queues.get(queueIndex);
        Worker worker = new Worker(queue, this, PREFIX_FOR_NAME_WORKER + (queueIndex + 1), activeWorkers);
        Thread thread = threadFactory.newThread(worker);
        workers.add(thread);
        thread.start();
        System.out.println("[Pool] New worker created. Total workers: " + workers.size());
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        if (shutdown) {
            rejectionPolicy.reject(command, this);
            return;
        }
        TaskQueue queue = balancer.selectQueue(workers.size());
        if (queue.offer(command)) {
            checkAndCreateSpareThreads();
            return;
        }
        synchronized (this) {
            if (workers.size() < maxPoolSize) {
                createWorker();
            }
        }
        queue = balancer.selectQueue(workers.size());
        if (queue.offer(command)) {
            return;
        }
        rejectionPolicy.reject(command, this);
    }

    private void checkAndCreateSpareThreads() {
        int spare = workers.size() - activeWorkers.get();
        if (spare < minSpareThreads && workers.size() < maxPoolSize) {
            createWorker();
        }
    }

    public void workerTerminated(Thread thread) {
        workers.remove(thread);
        System.out.println("[Pool] Worker removed. Remaining workers: " + workers.size());
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> future = new FutureTask<>(callable);
        execute(future);
        return future;
    }

    @Override
    public void shutdown() {
        shutdown = true;
        System.out.println("[Pool] Shutdown initiated. Waiting for remaining tasks...");
    }

    @Override
    public void shutdownNow() {
        shutdown = true;
        System.out.println("[Pool] ShutdownNow: interrupting all workers");
        synchronized (workers) {
            for (Thread t : workers) {
                t.interrupt();
            }
        }
    }

    public void awaitTermination() {
        System.out.println("[Pool] Awaiting termination of all workers");
        while (true) {
            Thread[] currentWorkers;
            synchronized (workers) {
                if (workers.isEmpty()) {
                    break;
                }
                currentWorkers = workers.toArray(new Thread[0]);
            }
            for (Thread t : currentWorkers) {
                if (t.isAlive()) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        System.out.println("[Pool] All workers terminated.");
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public boolean canWorkerTerminate() {
        return workers.size() > corePoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public synchronized void setRejectionPolicy(RejectionPolicy policy) {
        this.rejectionPolicy = policy;
    }

    public synchronized void setExecutionBalancer(ExecutionBalancer executionBalancer) {
        this.balancer = executionBalancer;
    }

    private void validateParams(int corePoolSize, int maxPoolSize, int minSpareThreads,
                                int queueSize, long keepAliveTime, TimeUnit timeUnit) {
        if (corePoolSize < 0
                || maxPoolSize <= 0
                || minSpareThreads < 0
                || corePoolSize > maxPoolSize
                || queueSize <= 0
                || keepAliveTime < 0
                || timeUnit == null
                || minSpareThreads > maxPoolSize) {
            throw new IllegalArgumentException("Invalid pool parameters");
        }
    }


}