import threadpool.impl.CustomThreadPool;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        CustomThreadPool pool = new CustomThreadPool(2, 6, 3, 5, 5, TimeUnit.SECONDS);
        for (int i = 0; i < 40; i++) {
            String taskId = "Task-" + i;
            try {
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        String threadName = Thread.currentThread().getName();
                        System.out.println(taskId + " started on " + threadName);
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        System.out.println(taskId + " finished on " + threadName);
                    }

                    @Override
                    public String toString() {
                        return taskId;
                    }
                });
            } catch (Exception e) {
                System.err.println("Error executing task: " + e.getMessage());
            }
        }

        Thread.sleep(8000);

        pool.shutdown();

        pool.awaitTermination();

    }
}