package eu.okaeri.platform.minecraft.task;

import eu.okaeri.commands.Commands;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.i18n.configs.extended.CustomMEOCI18n;
import eu.okaeri.i18n.extended.PrefixProvider;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.annotation.Messages;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.minecraft.commands.I18nCommandsTextHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CommandsI18nSetupTask implements ExecutionTask<OkaeriPlatform> {

    @Override
    public void execute(OkaeriPlatform platform) {

        Map<String, OkaeriConfig> configProviders = new LinkedHashMap<>();
        Map<String, CustomMEOCI18n<?>> i18nCommandsProviders = new LinkedHashMap<>();
        AtomicReference<PrefixProvider> prefixProvider = new AtomicReference<>();

        // read all available configs
        platform.getInjector().streamInjectableOf(OkaeriConfig.class)
            .filter(injectable -> injectable.getType().getAnnotation(Messages.class) == null)
            .forEach(injectable -> configProviders.put(injectable.getName(), injectable.getObject()));

        // read all available i18n
        platform.getInjector().allOf(CustomMEOCI18n.class)
            .forEach(injectable -> i18nCommandsProviders.put(injectable.getName(), injectable.getObject()));

        // get main i18n and resolve prefixProvider
        platform.getInjector().get("i18n", CustomMEOCI18n.class)
            .ifPresent(i18n -> prefixProvider.set(i18n.getPrefixProvider()));

        // update prefix provider of commands to the main one
        if (prefixProvider.get() != null) {
            platform.getInjector().get("i18n-platform-commands", CustomMEOCI18n.class)
                .ifPresent(i18n -> i18n.setPrefixProvider(prefixProvider.get()));
        }

        // inject all i18n into commands to allow ${key} access
        platform.getInjector().get("commands", Commands.class)
            .ifPresent(commands -> commands.textHandler(new I18nCommandsTextHandler(configProviders, i18nCommandsProviders)));
    }
}
