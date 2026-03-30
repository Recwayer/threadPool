package threadpool.config.balancer.impl;

import threadpool.config.balancer.ExecutionBalancer;
import threadpool.worker.TaskQueue;

import java.util.List;

public class LeastLoadedBalancer implements ExecutionBalancer {

    private final List<TaskQueue> queues;

    public LeastLoadedBalancer(List<TaskQueue> queues) {
        this.queues = queues;
    }

    @Override
    public TaskQueue selectQueue(int workerCount) {

        TaskQueue best = queues.get(0);
        for (int i = 1; i < workerCount; i++) {
            if (queues.get(i).size() < best.size()) {
                best = queues.get(i);
            }
        }
        return best;
    }
}
