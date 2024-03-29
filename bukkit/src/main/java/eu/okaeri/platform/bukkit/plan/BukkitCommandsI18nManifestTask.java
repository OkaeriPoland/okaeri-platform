package eu.okaeri.platform.bukkit.plan;

import eu.okaeri.i18n.configs.extended.CustomMEOCI18n;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.minecraft.commands.I18nCommandsMessages;

public class BukkitCommandsI18nManifestTask implements ExecutionTask<OkaeriBukkitPlugin> {

    @Override
    public void execute(OkaeriBukkitPlugin platform) {

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
