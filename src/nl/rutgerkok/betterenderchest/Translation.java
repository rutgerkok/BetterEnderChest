package nl.rutgerkok.betterenderchest;

import org.bukkit.ChatColor;

public class Translation {
	private final String originalString;

	public Translation(String string) {
		originalString = string;
	}

	/**
	 * Returns the string with proper ChatColors.
	 * @return The string with proper ChatColors.
	 */
	@Override
	public String toString() {
		return ChatColor.translateAlternateColorCodes('&', originalString);
	}

	/**
	 * Returns the string with proper ChatColors and with %s replaced.
	 * 
	 * @param args Objects to replace parts of the string with.
	 * @return The formatted string.
	 */
	public String toString(Object... args) {
		return ChatColor.translateAlternateColorCodes('&', String.format(originalString, args));
	}

	/**
	 * Gets the original string, with & and %.
	 * @return The original string.
	 */
	public String getOriginalString() {
		return originalString;
	}

	/**
	 * Returns true if, and only if, the string length is 0. 
	 * @return If the string length is 0.
	 */
	public boolean isEmpty() {
		return originalString.isEmpty();
	}
}
