package nl.rutgerkok.betterenderchest.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Contains methods that return {@link Executor}s. This class forms a bridge
 * between {@link BukkitScheduler Bukkit's scheduler} and {@link Executor Java's
 * executors}.
 *
 */
public final class BukkitExecutors {

    /**
     * A Bukkit executor, either for the server thread or for a worker thread.
     *
     */
    public static abstract class BukkitExecutor implements Executor {
        private BukkitExecutor() {
            // Only instantiated by enclosing class
        }

        public abstract BukkitTask executeTimer(int ticks, Runnable runnable);

        /**
         * Submits a task to this executor.
         *
         * @param <T>
         *            The result type.
         * @param task
         *            The task to run.
         * @return The future result of the task.
         */
        public <T> ListenableFuture<T> submit(final Callable<T> task) {
            final SettableFuture<T> future = SettableFuture.create();
            execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        future.set(task.call());
                    } catch (Exception e) {
                        future.setException(e);
                    }
                }
            });
            return future;
        }
    }

    private final Plugin plugin;

    private final BukkitExecutor serverThreadExecutor = new BukkitExecutor() {
        @Override
        public void execute(Runnable task) {
            Server server = plugin.getServer();
            if (server.isPrimaryThread()) {
                task.run();
            } else {
                server.getScheduler().runTask(plugin, task);
            }
        }

        @Override
        public BukkitTask executeTimer(int ticks, Runnable runnable) {
            return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, ticks, ticks);
        }
    };

    private final BukkitExecutor workerThreadExecutor = new BukkitExecutor() {
        @Override
        public void execute(Runnable task) {
            Server server = plugin.getServer();
            if (server.isPrimaryThread()) {
                server.getScheduler().runTaskAsynchronously(plugin, task);
            } else {
                task.run();
            }
        }

        @Override
        public BukkitTask executeTimer(int ticks, Runnable runnable) {
            return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, ticks, ticks);
        }
    };

    /**
     * Creates a new instance.
     * 
     * @param plugin
     *            The plugin, for scheduling purposes.
     */
    public BukkitExecutors(Plugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin");
    }

    /**
     * Executes a task on the server thread. When the
     * {@link Executor#execute(Runnable)} method is called from the server
     * thread, the code is run immediately, otherwise it is run during the next
     * tick.
     *
     * @return The executor.
     */
    public BukkitExecutor serverThreadExecutor() {
        return serverThreadExecutor;
    }

    /**
     * Executes a task on a worker thread. This can be any thread that isn't the
     * main server thread. When the {@link Executor#execute(Runnable)} method is
     * called from the server thread, the code is run later, otherwise it is run
     * immediately.
     *
     * @return The executor.
     */
    public BukkitExecutor workerThreadExecutor() {
        return workerThreadExecutor;
    }

}
