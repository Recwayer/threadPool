package threadpool.config.policy.impl;

import threadpool.impl.CustomThreadPool;
import threadpool.config.policy.RejectionPolicy;
import threadpool.exception.RejectedExecutionException;

public class AbortPolicy implements RejectionPolicy {

    @Override
    public void reject(Runnable task, CustomThreadPool pool) {
        System.out.println("[Rejected] Task " + task + " rejected.");
        throw new RejectedExecutionException("Task rejected due to overload");
    }
}
