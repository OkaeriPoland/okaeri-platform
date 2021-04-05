package eu.okaeri.platform.bukkit.commons.persistence;

import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.platform.core.persistence.PersistencePath;
import eu.okaeri.platform.core.persistence.flat.BasicFlatPersistence;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class YamlBukkitPersistence extends BasicFlatPersistence {

    private PersistencePath storageDir;

    public YamlBukkitPersistence(File storage) {
        super(new YamlBukkitConfigurer(), ".yml");
        this.storageDir = PersistencePath.of(storage);
    }

    public YamlBukkitPersistence(Plugin plugin) {
        this(new File(plugin.getDataFolder(), "storage"));
    }

    @Override
    public PersistencePath getPath() {
        return this.storageDir;
    }
}
