package threadpool.config.balancer.impl;

import threadpool.config.balancer.ExecutionBalancer;
import threadpool.worker.TaskQueue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalancer implements ExecutionBalancer {

    private final List<TaskQueue> queues;
    private final AtomicInteger index = new AtomicInteger();

    public RoundRobinBalancer(List<TaskQueue> queues) {
        this.queues = queues;
    }

    @Override
    public TaskQueue selectQueue(int workerCount) {
        if (workerCount == 0) {
            return queues.get(0);
        }
        int i = Math.abs(index.getAndIncrement() % workerCount);
        return queues.get(i);
    }
}