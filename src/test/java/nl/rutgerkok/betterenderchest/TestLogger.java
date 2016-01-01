package nl.rutgerkok.betterenderchest;

/**
 * Logger that outputs to {@link System#out} and {@link System#err}.
 */
public final class TestLogger implements PluginLogger {

    @Override
    public void debug(String string) {
        System.out.print("Debug: ");
        System.out.println(string);
    }

    @Override
    public void log(String message) {
        System.out.print("Log: ");
        System.out.println(message);
    }

    @Override
    public void severe(String message) {
        System.out.print("Error: ");
        System.err.println(message);
    }

    @Override
    public void severe(String message, Throwable thrown) {
        System.out.print("Error: ");
        System.err.println(message);
        thrown.printStackTrace(System.err);
    }

    @Override
    public void warning(String message) {
        System.out.print("Warning: ");
        System.out.println(message);
    }

}
