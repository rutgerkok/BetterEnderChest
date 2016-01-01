package nl.rutgerkok.betterenderchest.itemfilter;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import nl.rutgerkok.betterenderchest.PluginLogger;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Preconditions;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Reads configuration sections, converts them to item rules.
 */
public final class ItemFilterReader implements Function<Map<?, ?>, Predicate<ItemStack>> {

    private final PluginLogger logger;

    /**
     * Creates a new item filter reader.
     * 
     * @param logger
     *            The logger.
     * @throws NullPointerException
     *             If the logger is null.
     */
    public ItemFilterReader(PluginLogger logger) {
        this.logger = Preconditions.checkNotNull(logger);
    }

    /**
     * Parses a configuration section as an item rule.
     * 
     * @param configSection
     *            The section to parse.
     * @return The item rule.
     */
    @Override
    public Predicate<ItemStack> apply(Map<?, ?> configSection) {
        Object andSection = configSection.get("and");
        if (andSection instanceof Map<?, ?>) {
            return Predicates.and(fromCheckFor(configSection), apply((Map<?, ?>) andSection));
        }
        return fromCheckFor(configSection);
    }

    private Predicate<ItemStack> fromCheckFor(Map<?, ?> configSection) {
        String key = toStringOrNull(configSection.get("check"));

        if (key == null) {
            logger.warning("Invalid item stack rule: no 'check' found");
            return Predicates.alwaysFalse();
        }

        if (key.equals("customName")) {
            return getRuleForName(configSection);
        } else if (key.equals("lore")) {
            return getRuleForLore(configSection);
        } else {
            logger.warning("Invalid item rule: key '" + key + "' not recognized");
            return Predicates.alwaysFalse();
        }
    }

    private Predicate<ItemStack> getRuleForLore(Map<?, ?> configSection) {
        String value = null;
        Object listValues = configSection.get("for");
        if (listValues instanceof Collection<?>) {
            Collection<?> cListValues = (Collection<?>) listValues;
            if (!cListValues.isEmpty()) {
                value = Joiner.on('\n').join(cListValues);
            }
        } else if (listValues != null) {
            logger.warning(
                    "Invalid item rule: for value must be a list, " + listValues.getClass().getSimpleName() + " given");
            return Predicates.alwaysFalse();
        }

        String valueRegex = toStringOrNull(configSection.get("forRegex"));

        if (value != null && valueRegex == null) {
            return new LoreFilter(matchLiteral(value));
        } else if (value == null && valueRegex != null) {
            try {
                return new LoreFilter(Pattern.compile(valueRegex));
            } catch (PatternSyntaxException e) {
                logger.warning("Invalid regex in item rule: '" + e.getLocalizedMessage() + "' for regex " + valueRegex);
                return Predicates.alwaysFalse();
            }
        } else if (value == null && valueRegex == null) {
            logger.warning("Invalid item rule: no 'for' found");
            return Predicates.alwaysFalse();
        } else {
            logger.warning("Invalid item rule: both 'for' and 'forRegex' found");
            return Predicates.alwaysFalse();
        }
    }

    private Predicate<ItemStack> getRuleForName(Map<?, ?> configSection) {
        String value = toStringOrNull(configSection.get("for"));
        String valueRegex = toStringOrNull(configSection.get("forRegex"));
        if (value != null && valueRegex == null) {
            return new NameFilter(matchLiteral(value));
        } else if (value == null && valueRegex != null) {
            try {
                return new NameFilter(Pattern.compile(valueRegex));
            } catch (PatternSyntaxException e) {
                logger.warning("Invalid regex in item rule: '" + e.getLocalizedMessage() + "' for regex " + valueRegex);
                return Predicates.alwaysFalse();
            }
        } else if (value == null && valueRegex == null) {
            logger.warning("Invalid item rule: no 'for' found");
            return Predicates.alwaysFalse();
        } else {
            logger.warning("Invalid item rule: both 'for' and 'forRegex' found");
            return Predicates.alwaysFalse();
        }
    }

    private Pattern matchLiteral(String literal) {
        String regex = Pattern.quote(ChatColor.translateAlternateColorCodes('&', literal));
        return Pattern.compile("^" + regex + "$");
    }

    private String toStringOrNull(Object object) {
        if (object == null) {
            return null;
        }
        return object.toString();
    }
}
