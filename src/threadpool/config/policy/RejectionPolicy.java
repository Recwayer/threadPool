package threadpool.config.policy;

import threadpool.impl.CustomThreadPool;

public interface RejectionPolicy {
    void reject(Runnable task, CustomThreadPool pool);
}
