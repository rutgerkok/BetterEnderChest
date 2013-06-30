package nl.rutgerkok.betterenderchest.io;

/**
 * A callback interface, which is called on the main thread after the
 * asynchronous task has done it's work.
 * 
 * @param <T>
 *            The value that was looked up.
 */
public interface Consumer<T> {
    void consume(T t);
}
