package threadpool.config.balancer;

import threadpool.worker.TaskQueue;

public interface ExecutionBalancer {
    TaskQueue selectQueue(int workerCount);
}
