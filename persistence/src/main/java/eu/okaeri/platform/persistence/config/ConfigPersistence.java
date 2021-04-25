package eu.okaeri.platform.persistence.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.Persistence;
import eu.okaeri.platform.persistence.PersistencePath;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
public abstract class ConfigPersistence implements Persistence<ConfigDocument> {

    @Getter private final PersistencePath basePath;
    @Getter private final ConfigConfigurerProvider configurerProvider;
    @Getter private final OkaeriSerdesPack[] serdesPacks;

    @Override
    public ConfigDocument read(PersistencePath collection, PersistencePath path) {
        return this.load(this.createDocument(collection, path), collection, path);
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

    public ConfigDocument createDocument(PersistencePath collection, PersistencePath path) {
        ConfigDocument config = ConfigManager.create(ConfigDocument.class);
        config.withConfigurer(this.configurerProvider.get(), this.serdesPacks);
        config.setSaver(document -> this.write(collection, path, document));
        return config;
    }

    public abstract ConfigDocument load(ConfigDocument document, PersistencePath collection, PersistencePath path);
}

