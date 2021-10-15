package eu.okaeri.platform.bukkit.i18n;

import eu.okaeri.commands.handler.text.TextHandler;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class I18nCommandsTextHandler implements TextHandler {

    private static final Pattern CONTEXT_KEY_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9-.]+)}");
    private static final Pattern STATIC_KEY_PATTERN = Pattern.compile("#\\{([a-zA-Z0-9-.]+)}");

    private final Set<BI18n> i18n;

    public I18nCommandsTextHandler(@NonNull BI18n i18n) {
        this(new HashSet<>(Collections.singletonList(i18n)));
    }

    @Override
    public String resolve(@NonNull String text) {
        return text; // TODO: ???
    }

    @Override
    public String resolve(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull String text) {

        Set<String> contextKeys = findKeys(CONTEXT_KEY_PATTERN, text);
        if (!contextKeys.isEmpty()) {
            return text;
        }

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        for (String key : contextKeys) {
            for (BI18n i18n : this.i18n) {
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
