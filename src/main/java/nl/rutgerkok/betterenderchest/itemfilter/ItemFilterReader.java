package nl.rutgerkok.betterenderchest.itemfilter;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import nl.rutgerkok.betterenderchest.PluginLogger;
import nl.rutgerkok.betterenderchest.util.MaterialParser;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
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
        } else if (key.equals("itemType")) {
            return getRuleForItemType(configSection);
        } else {
            logger.warning("Invalid item rule: key '" + key + "' not recognized");
            return Predicates.alwaysFalse();
        }
    }

    private Predicate<ItemStack> getRuleForItemType(Map<?, ?> configSection) {
        String materialString = toStringOrNull(configSection.get("for"));
        if (materialString == null) {
            logger.warning("Invalid item rule: no 'for' found");
            return Predicates.alwaysFalse();
        }

        Material material = MaterialParser.matchMaterial(materialString);
        if (material == null) {
            logger.warning("Invalid item rule: '" + materialString + "' is not a valid material");
            return Predicates.alwaysFalse();
        }

        return new ItemTypeFilter(material);
    }

    private Predicate<ItemStack> getRuleForLore(Map<?, ?> configSection) {
        boolean ignoreColors = hasIgnoreFlag(configSection, "color");
        boolean ignoreCase = hasIgnoreFlag(configSection, "case");

        String value = toMultilineStringOrNull(configSection.get("for"));
        String valueRegex = toStringOrNull(configSection.get("forRegex"));

        if (value != null && valueRegex == null) {
            return new LoreFilter(patternFromLiteral(value, ignoreCase), ignoreColors);
        } else if (value == null && valueRegex != null) {
            try {
                return new LoreFilter(patternFromRegex(valueRegex, ignoreCase), ignoreColors);
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
        boolean ignoreColors = hasIgnoreFlag(configSection, "color");
        boolean ignoreCase = hasIgnoreFlag(configSection, "case");
        String value = toStringOrNull(configSection.get("for"));
        String valueRegex = toStringOrNull(configSection.get("forRegex"));

        if (value != null && valueRegex == null) {
            return new NameFilter(patternFromLiteral(value, ignoreCase), ignoreColors);
        } else if (value == null && valueRegex != null) {
            try {
                return new NameFilter(patternFromRegex(valueRegex, ignoreCase), ignoreColors);
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

    private boolean hasIgnoreFlag(Map<?, ?> configSection, String ignoreFlag) {
        Object ignoring = configSection.get("ignoring");
        if (ignoreFlag.equals(ignoring)) {
            return true;
        }
        if (ignoring instanceof Collection<?>) {
            return ((Collection<?>) ignoring).contains(ignoreFlag);
        }
        return false;
    }

    private Pattern patternFromLiteral(String literal, boolean ignoreCase) {
        literal = ChatColor.translateAlternateColorCodes('&', literal);
        int flags = 0;
        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
            flags |= Pattern.UNICODE_CASE;
        }
        return Pattern.compile("^" + Pattern.quote(literal) + "$", flags);
    }

    private Pattern patternFromRegex(String regex, boolean ignoreCase) {
        int flags = 0;
        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
            flags |= Pattern.UNICODE_CASE;
        }
        return Pattern.compile(regex, flags);
    }

    private String toMultilineStringOrNull(Object value) {
        if (value instanceof Collection<?>) {
            return Joiner.on('\n').join((Collection<?>) value);
        }
        return null;
    }

    private String toStringOrNull(Object object) {
        if (object == null) {
            return null;
        }
        return object.toString();
    }
}
