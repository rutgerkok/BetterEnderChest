package nl.rutgerkok.betterenderchest;

/**
 * The logger for the plugin.
 */
public interface PluginLogger {

    /**
     * Logs a debug message.
     * 
     * @param string
     *            The string to print.
     */
    void debug(String string);

    /**
     * Logs a message with normal importance. Message will be prefixed with the
     * plugin name between square brackets.
     * 
     * @param message
     *            The message to show.
     */
    void log(String message);

    /**
     * Logs an error. Message will be prefixed with the plugin name between
     * square brackets.
     * 
     * @param message
     *            The message to show.
     */
    void severe(String message);

    /**
     * Logs an error with the exception. Message will be prefixed with the
     * plugin name between square brackets.
     * 
     * @param message
     *            The message to show.
     * @param thrown
     *            The exception that caused the error.
     */
    void severe(String message, Throwable thrown);

    /**
     * Logs a warning. Message will be prefixed with the plugin name between
     * square brackets.
     * 
     * @param message
     *            The message to show.
     */
    void warning(String message);

}