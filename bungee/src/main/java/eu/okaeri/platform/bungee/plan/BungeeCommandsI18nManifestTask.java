package eu.okaeri.platform.bungee.plan;

import eu.okaeri.i18n.configs.extended.CustomMEOCI18n;
import eu.okaeri.platform.bungee.OkaeriBungeePlugin;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.minecraft.commands.I18nCommandsMessages;

public class BungeeCommandsI18nManifestTask implements ExecutionTask<OkaeriBungeePlugin> {

    @Override
    public void execute(OkaeriBungeePlugin platform) {

        BeanManifest manifest = platform.getInjector().get("manifest", BeanManifest.class)
            .orElseThrow(() -> new RuntimeException("Cannot hook i18n-platform-commands without manifest being present!"));

        ClassLoader classLoader = platform.getClass().getClassLoader();
        ComponentCreator creator = platform.getCreator();

        if (platform.getInjector().get("i18n-platform-commands", CustomMEOCI18n.class).isPresent()) {
            return;
        }

        manifest.withDepend(BeanManifest.of(classLoader, I18nCommandsMessages.class, creator, false).name("i18n-platform-commands"));
    }
}
