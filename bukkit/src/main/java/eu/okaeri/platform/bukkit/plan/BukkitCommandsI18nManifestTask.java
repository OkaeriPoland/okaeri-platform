package eu.okaeri.platform.bukkit.plan;

import eu.okaeri.injector.Injectable;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.minecraft.i18n.I18nCommandsMessages;

public class BukkitCommandsI18nManifestTask implements ExecutionTask<OkaeriBukkitPlugin> {

    @Override
    public void execute(OkaeriBukkitPlugin platform) {

        BeanManifest manifest = platform.getInjector().getInjectable("manifest", BeanManifest.class)
                .map(Injectable::getObject)
                .orElseThrow(() -> new RuntimeException("Cannot hook i18n-platform-commands without manifest being present!"));

        manifest.withDepend(BeanManifest.of(I18nCommandsMessages.class, platform.getCreator(), false).name("i18n-platform-commands"));
    }
}
