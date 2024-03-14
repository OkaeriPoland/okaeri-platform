package eu.okaeri.platform.minecraft.commands;

import eu.okaeri.commands.handler.text.TextHandler;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.migrate.view.RawConfigView;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.i18n.configs.extended.CustomMEOCI18n;
import eu.okaeri.validator.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class I18nCommandsTextHandler implements TextHandler {

    protected static final Pattern CONTEXT_VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    protected static final Pattern STATIC_VAR_PATTERN = Pattern.compile("#\\{([^}]+)}");

    protected final Map<String, OkaeriConfig> configs;
    protected final Map<String, CustomMEOCI18n<?>> i18n;

    @Deprecated
    public I18nCommandsTextHandler(@NonNull Map<String, CustomMEOCI18n<?>> i18n) {
        this(new LinkedHashMap<>(), i18n);
    }

    public I18nCommandsTextHandler(@NonNull Map<String, OkaeriConfig> configs, @NonNull Map<String, CustomMEOCI18n<?>> i18n) {
        this.configs = new LinkedHashMap<>(configs);
        this.i18n = new LinkedHashMap<>(i18n);
    }

    @Override
    public String resolve(@NonNull String text) {

        Set<String> variables = findVariables(STATIC_VAR_PATTERN, text);
        if (variables.isEmpty()) {
            return text;
        }

        for (String variable : variables) {

            List<OkaeriConfig> sources = new ArrayList<>();
            if (variable.contains(":")) {

                String[] sourceParts = variable.split(":", 2);
                String sourceName = sourceParts[0];

                if (this.configs.containsKey(sourceName)) {
                    sources.add(this.configs.get(sourceName));
                    variable = sourceParts[1];
                } else {
                    throw new IllegalArgumentException("Unknown config specified: " + variable);
                }
            } else {
                sources.addAll(this.configs.values());
            }

            for (OkaeriConfig config : sources) {
                Object value = new RawConfigView(config).get(variable);
                if (value != null) {
                    String resolved = config.getConfigurer().resolveType(value,
                        GenericsDeclaration.of(value),
                        String.class,
                        GenericsDeclaration.of(String.class),
                        SerdesContext.of(config.getConfigurer())
                    );
                    text = text.replace("#{" + variable + "}", resolved);
                    break;
                }
            }
        }

        return text;
    }

    @Override
    public String resolve(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull String text) {

        Set<String> variables = findVariables(CONTEXT_VAR_PATTERN, text);
        if (variables.isEmpty()) {
            return text;
        }

        Object sender = data.get("sender");
        if (sender == null) {
            return text;
        }

        for (String variable : variables) {

            List<CustomMEOCI18n<?>> sources = new ArrayList<>();
            if (variable.contains(":")) {

                String[] sourceParts = variable.split(":", 2);
                String sourceName = sourceParts[0];

                if (this.i18n.containsKey(sourceName)) {
                    sources.add(this.i18n.get(sourceName));
                    variable = sourceParts[1];
                } else {
                    throw new IllegalArgumentException("Unknown i18n specified: " + variable);
                }
            } else {
                sources.addAll(this.i18n.values());
            }

            for (CustomMEOCI18n<?> i18n : sources) {
                String value = i18n.get(sender, variable).raw();
                if (this.isValueValid(value, variable)) {
                    text = text.replace("${" + variable + "}", value);
                    break;
                }
            }
        }

        return text;
    }

    protected boolean isValueValid(@Nullable String value, @NonNull String key) {
        return (value != null) && !("<" + key + ">").equals(value);
    }

    protected static Set<String> findVariables(@NonNull Pattern pattern, @NonNull String text) {

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
