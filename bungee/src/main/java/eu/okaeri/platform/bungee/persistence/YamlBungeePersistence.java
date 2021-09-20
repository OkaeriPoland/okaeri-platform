package eu.okaeri.platform.bungee.persistence;

import eu.okaeri.configs.yaml.bungee.YamlBungeeConfigurer;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.flat.FlatPersistence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlBungeePersistence {

    public static DocumentPersistence of(@NonNull File storage) {
        return new DocumentPersistence(new FlatPersistence(storage, ".yml"), YamlBungeeConfigurer::new);
    }

    public static DocumentPersistence of(@NonNull Plugin plugin) {
        return of(new File(plugin.getDataFolder(), "storage"));
    }
}
