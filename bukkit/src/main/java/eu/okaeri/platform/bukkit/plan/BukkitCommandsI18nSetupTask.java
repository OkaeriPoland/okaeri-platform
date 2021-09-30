package eu.okaeri.platform.bukkit.plan;

import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import eu.okaeri.platform.bukkit.i18n.I18nCommandsTextHandler;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.minecraft.i18n.I18nPrefixProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class BukkitCommandsI18nSetupTask implements ExecutionTask<OkaeriBukkitPlugin> {

    @Override
    public void execute(OkaeriBukkitPlugin platform) {

        Set<BI18n> i18nCommandsProviders = new HashSet<>();
        AtomicReference<I18nPrefixProvider> prefixProvider = new AtomicReference<>();
        platform.getInjector().getInjectable("i18n", BI18n.class).ifPresent(i18n -> {
            BI18n bi18n = i18n.getObject();
            prefixProvider.set(bi18n.getPrefixProvider());
            i18nCommandsProviders.add(bi18n);
        });

        platform.getInjector().getInjectable("i18n-platform-commands", BI18n.class).ifPresent(i18n -> {
            BI18n bi18n = i18n.getObject();
            I18nPrefixProvider i18nPrefixProvider = prefixProvider.get();
            if (i18nPrefixProvider != null) {
                bi18n.setPrefixProvider(i18nPrefixProvider);
            }
            i18nCommandsProviders.add(bi18n);
        });

        platform.getInjector().getInjectable("commands", CommandsBukkit.class).ifPresent(commands -> {
            CommandsBukkit commandsBukkit = commands.getObject();
            commandsBukkit.textHandler(new I18nCommandsTextHandler(i18nCommandsProviders));
        });
    }
}
