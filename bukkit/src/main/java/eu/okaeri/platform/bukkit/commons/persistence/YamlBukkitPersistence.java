package eu.okaeri.platform.bukkit.commons.persistence;

import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.platform.persistence.flat.BasicFlatPersistence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlBukkitPersistence {

    public static BasicFlatPersistence of(File storage) {
        return new BasicFlatPersistence(storage, ".yml", YamlBukkitConfigurer::new, new SerdesBukkit());
    }

    public static BasicFlatPersistence of(Plugin plugin) {
        return of(new File(plugin.getDataFolder(), "storage"));
    }
}
