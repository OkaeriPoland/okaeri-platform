package eu.okaeri.platform.persistence.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.Persistence;
import eu.okaeri.platform.persistence.PersistencePath;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

// build to store data in the PLUGIN_DIR/storage/*
@AllArgsConstructor
public abstract class ConfigPersistence implements Persistence<ConfigDocument> {

    @Getter private final PersistencePath basePath;
    @Getter private final Configurer configurer;
    @Getter private final OkaeriSerdesPack[] serdesPacks;

    @Override
    public ConfigDocument read(PersistencePath collection, PersistencePath path) {
        PersistencePath fullPath = this.toFullPath(collection, path);
        System.out.println("READ " + fullPath);
        ConfigDocument config = ConfigManager.create(ConfigDocument.class);
        config.withConfigurer(this.configurer, this.serdesPacks);
        config.setSaver(document -> this.write(collection, path, document));
        this.documentCreated(config, collection, path);
        return this.load(config, collection, path);
    }

    @Override
    public Collection<ConfigDocument> readAll(PersistencePath collection) {
        throw new RuntimeException("Not implemented yet");
    }

    public PersistencePath toFullPath(PersistencePath collection, PersistencePath path) {
        return this.getBasePath().sub(collection).sub(this.convertPath(path));
    }

    public PersistencePath convertPath(PersistencePath path) {
        return path;
    }

    public void documentCreated(ConfigDocument document, PersistencePath collection, PersistencePath path) {
    }

    public abstract ConfigDocument load(ConfigDocument document, PersistencePath collection, PersistencePath path);
}

