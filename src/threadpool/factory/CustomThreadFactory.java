package threadpool.factory;

import java.util.concurrent.ThreadFactory;

public class CustomThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        String name = r.toString();
        System.out.println("[ThreadFactory] Creating new thread: " + name);
        return new Thread(r, name);
    }
}
