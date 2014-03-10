package nl.rutgerkok.betterenderchest.io;

/**
 * Represents an error that occured during saving and loading.
 * 
 */
public class SaveAndLoadError {

    private final Throwable cause;
    private final String message;

    /**
     * Creates a new error.
     * 
     * @param message
     *            Message for the server admin.
     * @param cause
     *            The stacktrace that caused the error.
     */
    public SaveAndLoadError(String message, Throwable cause) {
        this.cause = cause;
        this.message = message;
    }

    /**
     * Gets the cause of the error, for the server admin.
     * 
     * @return The cause.
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Gets the message for the server admin about what went wrong.
     * 
     * @return The message.
     */
    public String getMessage() {
        return message;
    }
}
