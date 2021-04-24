package eu.okaeri.platform.persistence.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.Persistence;
import eu.okaeri.platform.persistence.PersistencePath;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Collection;

// build to store data in the PLUGIN_DIR/storage/*
@AllArgsConstructor
public abstract class ConfigPersistence implements Persistence<ConfigDocument> {

    @Getter private final Configurer configurer;
    @Getter private final OkaeriSerdesPack[] serdesPacks;

    @Override
    @SuppressWarnings("unchecked")
    public ConfigDocument read(PersistencePath path) {
        return this.keyToConfig(path);
    }

    @Override
    public Collection<ConfigDocument> readAll() {
        throw new RuntimeException("Not implemented yet");
    }

    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected ConfigDocument keyToConfig(PersistencePath path) {
        PersistencePath fullPath = this.toFullPath(path);
        ConfigDocument config = ConfigManager.create(ConfigDocument.class);
        config.withConfigurer(this.configurer, this.serdesPacks);
        config.setSaver(document -> this.write(path, document));
        this.documentCreated(config, fullPath);
        return this.load(config, fullPath);
    }

    public PersistencePath toFullPath(PersistencePath path) {
        return this.getBasePath().sub(this.convertPath(path));
    }

    public PersistencePath convertPath(PersistencePath path) {
        return path;
    }

    public void documentCreated(ConfigDocument document, PersistencePath fullPath) {
    }

    public abstract ConfigDocument load(ConfigDocument document, PersistencePath fullPath);
}

