package nl.rutgerkok.betterenderchest.registry;

public interface Registration {
	/**
	 * Gets the name of the registration, usually used somewhere in the
	 * config.yml.
	 * 
	 * @return The name of the registration.
	 */
	public String getName();

	/**
	 * Gets whether this registration can be used at the moment. It can check
	 * whether another plugin is available, for example.
	 * 
	 * @return Whether this registration can be used at the moment.
	 */
	public boolean isAvailable();

	/**
	 * This will return true if this registration is a fallback. If it is a
	 * fallback, other registrations will have priority when using
	 * {@link Registry#selectAvailableRegistration()}.
	 * 
	 * @return Whether this registration is a fallback.
	 */
	public boolean isFallback();
}
