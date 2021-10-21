package eu.okaeri.platform.minecraft.task;

import eu.okaeri.commands.Commands;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.minecraft.i18n.I18nCommandsTextHandler;
import eu.okaeri.platform.minecraft.i18n.I18nPrefixProvider;
import eu.okaeri.platform.minecraft.i18n.MI18n;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CommandsI18nSetupTask implements ExecutionTask<OkaeriPlatform> {

    @Override
    public void execute(OkaeriPlatform platform) {

        Map<String, MI18n> i18nCommandsProviders = new LinkedHashMap<>();
        AtomicReference<I18nPrefixProvider> prefixProvider = new AtomicReference<>();

        // read all available i18n
        platform.getInjector().allOf(MI18n.class)
                .forEach(injectable -> i18nCommandsProviders.put(injectable.getName(), injectable.getObject()));

        // get main i18n and resolve prefixProvider
        platform.getInjector().get("i18n", MI18n.class)
                .ifPresent(i18n -> prefixProvider.set(i18n.getPrefixProvider()));

        // update prefix provider of commands to the main one
        if (prefixProvider.get() != null) {
            platform.getInjector().get("i18n-platform-commands", MI18n.class)
                    .ifPresent(i18n -> i18n.setPrefixProvider(prefixProvider.get()));
        }

        // inject all i18n into commands to allow ${key} access
        platform.getInjector().get("commands", Commands.class)
                .ifPresent(commands -> commands.textHandler(new I18nCommandsTextHandler(i18nCommandsProviders)));
    }
}
