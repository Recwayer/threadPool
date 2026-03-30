package threadpool.config.policy.impl;

import threadpool.impl.CustomThreadPool;
import threadpool.config.policy.RejectionPolicy;

public class CallerRunsPolicy implements RejectionPolicy {

    @Override
    public void reject(Runnable task, CustomThreadPool pool) {
        System.out.println("[Rejected] Running task in caller thread: " + task);
        if (!pool.isShutdown()) {
            task.run();
        }
    }
}
