package nl.rutgerkok.betterenderchest.registry;

public interface Registration {
    public enum Priority {
        /** Used only if nothing else is available */
        FALLBACK,
        /** Used to give higher priority than most registrations */
        HIGH,
        /** Used to give lower priority than most registrations */
        LOW,
        /** Most registrations should use this */
        NORMAL;
    }

    /**
     * Gets the name of the registration, usually used somewhere in the
     * config.yml.
     * 
     * @return The name of the registration.
     */
    String getName();

    /**
     * Gets the priority of this registration. When using
     * {@link Registry#selectAvailableRegistration()}, it will prefer
     * registrations with a higher priority.
     * 
     * @return The priority of this registration.
     */
    Priority getPriority();

    /**
     * Gets whether this registration can be used at the moment. It can check
     * whether another plugin is available, for example.
     * 
     * @return Whether this registration can be used at the moment.
     */
    boolean isAvailable();
}
