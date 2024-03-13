package eu.okaeri.platform.minecraft.commands;

import eu.okaeri.commands.handler.text.TextHandler;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.i18n.configs.extended.CustomMEOCI18n;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class I18nCommandsTextHandler implements TextHandler {

    private static final Pattern CONTEXT_KEY_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern STATIC_KEY_PATTERN = Pattern.compile("#\\{([^}]+)}");

    protected final Map<String, CustomMEOCI18n<?>> i18n;

    @Override
    public String resolve(@NonNull String text) {
        return text; // TODO: ???
    }

    @Override
    public String resolve(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull String text) {

        Set<String> contextKeys = findKeys(CONTEXT_KEY_PATTERN, text);
        if (contextKeys.isEmpty()) {
            return text;
        }

        Object sender = data.get("sender");
        if (sender == null) {
            return text;
        }

        for (String key : contextKeys) {

            List<CustomMEOCI18n<?>> sources = new ArrayList<>();
            if (key.contains(":")) {

                String[] i18nParts = key.split(":", 2);
                String i18nName = i18nParts[0];

                if (this.i18n.containsKey(i18nName)) {
                    sources.add(this.i18n.get(i18nName));
                    key = i18nParts[1];
                } else {
                    throw new IllegalArgumentException("Unknown i18n specified: " + key);
                }
            } else {
                sources.addAll(this.i18n.values());
            }

            for (CustomMEOCI18n<?> i18n : sources) {
                String value = i18n.get(sender, key).raw();
                if (this.isValid(value, key)) {
                    text = text.replace("${" + key + "}", value);
                    break;
                }
            }
        }

        return text;
    }

    private boolean isValid(String value, @NonNull String key) {
        return (value != null) && !("<" + key + ">").equals(value);
    }

    private static Set<String> findKeys(Pattern pattern, String text) {

        if (!text.contains("#") && !text.contains("$")) {
            return Collections.emptySet();
        }

        Set<String> keys = new HashSet<>();
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            keys.add(matcher.group(1));
        }

        return keys;
    }
}
