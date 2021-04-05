package eu.okaeri.platform.core.persistence.flat;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.core.persistence.Persistence;
import eu.okaeri.platform.core.persistence.PersistencePath;
import eu.okaeri.platform.core.persistence.document.Document;
import eu.okaeri.platform.core.persistence.document.SimpleDocument;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Collection;

// build to store data in the PLUGIN_DIR/storage/*
public abstract class BasicFlatPersistence implements Persistence<Document> {

    @Getter private final Configurer configurer;
    @Getter private final OkaeriSerdesPack[] serdesPacks;
    @Getter private final String fileSuffix;

    public BasicFlatPersistence(Configurer configurer, String fileSuffix, OkaeriSerdesPack... serdesPacks) {
        this.configurer = configurer;
        this.serdesPacks = serdesPacks;
        this.fileSuffix = fileSuffix;
    }

    @Override
    public boolean exists(PersistencePath path) {
        return this.getPath().sub(path.append(this.fileSuffix)).toFile().exists();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Document read(PersistencePath path) {
        return new SimpleDocument(this.keyToConfig(path).asMap(this.configurer, true));
    }

    public <T extends OkaeriConfig> T readInto(PersistencePath key, Class<T> configClazz) {
        T config = ConfigManager.create(configClazz);
        config.withConfigurer(this.getConfigurer(), this.getSerdesPacks());
        config.withBindFile(this.getPath().sub(key.append(this.fileSuffix)).toFile());
        this.read(key).getData().forEach(config::set);
        return config;
    }

    @Override
    public boolean write(PersistencePath key, Document mappable) {
        FlatConfig config = this.keyToConfig(key);
        mappable.getData().forEach(config::set);
        config.save();
        return true;
    }

    @Override
    public Collection<Document> readAll() {
        throw new RuntimeException("Not implemented yet");
    }

    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private FlatConfig keyToConfig(PersistencePath key) {

        PersistencePath fullPath = this.getPath().sub(key.append(this.fileSuffix));
        File dir = fullPath.group().toFile();
        dir.mkdirs();

        File bindFile = fullPath.toFile();
        bindFile.createNewFile();

        return ConfigManager.create(FlatConfig.class, (it) -> {
            it.withConfigurer(this.configurer, this.serdesPacks);
            it.withBindFile(bindFile);
            it.load();
        });
    }
}

// undocumented feature of okaeri-configs :woah:
class FlatConfig extends OkaeriConfig {
}
