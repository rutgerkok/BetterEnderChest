package nl.rutgerkok.betterenderchest.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * A registry for things. It can be used for many types of regiters. Some
 * examples include:
 * 
 * <ul>
 * <li>Multiple options, one will be automatically selected based on the current
 * environment. Used for NMSHandler and ProtectionBridge.</li>
 * <li>Multiple options, user chooses. Uses for InventoryImporter and
 * BaseCommand.</li>
 * </ul>
 * 
 * If you are developing another plugin, you should register your stuff in the
 * onLoad method of JavaPlugin.
 * <p />
 * Names will be loosely matched, so <code>ENDER_CHEST</code> is the same as
 * <code>enderChest</code>, <code>ender chest</code> and even
 * <code>E_n  De-Rc H-eST</code>
 * 
 * @param <T>
 *            Type to register.
 */
public class Registry<T extends Registration> {
    private Map<String, T> registered = new HashMap<String, T>();
    private T selected;
    private boolean selectedIsInited;

    /*
     * Filters a name for easy key matching.
     */
    private String filterName(String string) {
        return string.toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
    }

    /**
     * Returns an available fallback, for example for when
     * {@link #getAvailableRegistration(String)} returned null.
     * 
     * @return An available fallback.
     */
    public T getAvailableFallback() {
        for (T registration : registered.values()) {
            if (registration.isAvailable() && registration.isFallback()) {
                return registration;
            }
        }
        return null;
    }

    /**
     * Returns an available registration. Registrations that return false in
     * their {@link Registration#isAvailable()} won't be returned. Returns null
     * if no registration is found. Registration can be a fallback.
     * 
     * @param name
     *            Name of the registration.
     * @return An available registration.
     */
    public T getAvailableRegistration(String name) {
        T registration = registered.get(filterName(name));
        if (registration != null && registration.isAvailable()) {
            return registration;
        }
        return null;
    }

    /**
     * Returns a registration. It is possible that the registration cannot be
     * used. Returns null if no registration with that name exists. Registration
     * can be a fallback.
     * 
     * @param name
     *            Name of the registration.
     * @return A registration.
     */
    public T getRegistration(String name) {
        return registered.get(filterName(name));
    }

    /**
     * Returns all registrations. The collection cannot be changed.
     * 
     * @return All registrations.
     */
    public Collection<T> getRegistrations() {
        return Collections.unmodifiableCollection(registered.values());
    }

    /**
     * Returns the selected registration, or null if there isn't one.
     * 
     * @return The selected registration.
     */
    public T getSelectedRegistration() {
        if (!selectedIsInited) {
            throw new RuntimeException("Cannot retrieve selected registration: not registration has been selected!");
        }
        return selected;
    }

    /**
     * Registers a registration. The registration cannot be null.
     * 
     * @param registration
     */
    public void register(T registration) {
        Validate.notNull(registration, "Null registrations are not accepted");
        registered.put(filterName(registration.getName()), registration);
    }

    /**
     * Selects the first registration that is available, so it can later be
     * retrieved with {@link #getSelectedRegistration()}. Can be used for
     * registers where usually one registration is available (NMS handlers, for
     * example). Non-fallbacks will take priority over fallback registrations.
     * 
     * @return The just selected registration, or null if no registration was
     *         available.
     */
    public T selectAvailableRegistration() {
        selectedIsInited = true;

        T selected = null;
        T fallback = null;
        for (T registration : registered.values()) {
            if (registration.isAvailable()) {
                if (registration.isFallback()) {
                    fallback = registration;
                } else {
                    selected = registration;
                }
                break;
            }
        }
        if (selected != null) {
            this.selected = selected;
            return selected;
        } else {
            this.selected = fallback;
            return fallback;
        }
    }

    /**
     * Selects the available registration with the given name, so it can later
     * be retrieved with {@link #getSelectedRegistration()}. Registrations that
     * return false in their {@link Registration#isAvailable()} won't be
     * selected.
     * 
     * @param name
     *            Name of the registration.
     * @return The registration that was just selected.
     */
    public T selectAvailableRegistration(String name) {
        selectedIsInited = true;

        selected = getAvailableRegistration(name);
        return selected;
    }

    /**
     * Selects the given registration. If the registration is not registered, it
     * will be done automatically. Registration can be null.
     * 
     * @param registration
     *            The registration.
     * @return The registration you just selected.
     */
    public T selectRegistration(T registration) {
        selectedIsInited = true;

        if (registration != null && !registered.containsValue(registration)) {
            register(registration);
        }
        selected = registration;
        return selected;
    }
}
