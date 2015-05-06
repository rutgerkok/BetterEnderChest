package nl.rutgerkok.betterenderchest.util;

import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Similar to {@link SettableFuture}, but adds an
 * {@link #updateUsing(ListenableFuture)} method.
 *
 * @param <T>
 *            The result type returned by this Future's <tt>get</tt> method.
 *
 * @see #create()
 */
public class UpdateableFuture<T> extends AbstractFuture<T> {

    /**
     * Creates a new {@link UpdateableFuture}.
     * 
     * @return The object.
     */
    public static <T> UpdateableFuture<T> create() {
        return new UpdateableFuture<T>();
    }

    private UpdateableFuture() {
        // Use the factory method
    }

    /**
     * Sets the value of this future. This object will then be marked as
     * completed.
     * 
     * @param value
     *            The value.
     * @return True when the value was changed, false if the future was already
     *         completed or failed.
     */
    @Override
    public boolean set(T value) {
        return super.set(value);
    }

    /**
     * Marks the future as failed.
     *
     * @param throwable
     *            The cause of the failure.
     * @return True if the exception was set, false if the future was already
     *         completed or failed.
     */
    @Override
    public boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }

    /**
     * Links the state of this future with the given future. This will make this
     * future mirror its state with the given future.
     *
     * <p>
     * This method will do nothing if this future was already {@link #isDone()
     * done}. If this future is attached to multiple other futures, one will
     * "win", but it is unspecified which one.
     *
     * @param future
     *            The future.
     */
    public void updateUsing(final ListenableFuture<T> future) {
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    set(future.get());
                } catch (InterruptedException e) {
                    setException(e);
                } catch (ExecutionException e) {
                    setException(e.getCause());
                }
            }
        }, MoreExecutors.sameThreadExecutor());
    }
}
