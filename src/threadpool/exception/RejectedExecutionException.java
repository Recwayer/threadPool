package threadpool.exception;

public class RejectedExecutionException extends RuntimeException{
    public RejectedExecutionException(String message) {
        super(message);
    }
}
