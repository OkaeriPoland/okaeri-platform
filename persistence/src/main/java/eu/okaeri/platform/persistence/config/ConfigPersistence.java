package eu.okaeri.platform.persistence.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.TransformerRegistry;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.platform.persistence.Persistence;
import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistencePath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public abstract class ConfigPersistence implements Persistence<ConfigDocument> {

    @Getter private final PersistencePath basePath;
    @Getter private final ConfigConfigurerProvider configurerProvider;
    @Getter private final OkaeriSerdesPack[] serdesPacks;
    @Getter private final Map<String, PersistenceCollection> knownCollections = new HashMap<>();
    private TransformerRegistry transformerRegistry;

    public void registerCollection(PersistenceCollection collection) {
        if (this.transformerRegistry == null) {
            TransformerRegistry registry = new TransformerRegistry();
            Stream.concat(Stream.of(new StandardSerdes(), new SerdesCommons()), Stream.of(this.serdesPacks))
                    .forEach(pack -> pack.register(registry));
            this.transformerRegistry = registry;
        }
        this.knownCollections.put(collection.getValue(), collection);
    }

    public void checkCollectionRegistered(PersistenceCollection collection) {
        if (this.knownCollections.containsKey(collection.getValue())) {
            return;
        }
        throw new IllegalArgumentException("cannot use unregistered collection: " + collection);
    }

    @Override
    public ConfigDocument read(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.read(this.createDocument(collection, path), collection, path);
    }

    public PersistencePath toFullPath(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.getBasePath().sub(collection).sub(this.convertPath(path));
    }

    public PersistencePath convertPath(PersistencePath path) {
        return path;
    }

    public ConfigDocument createDocument(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        ConfigDocument config = ConfigManager.create(ConfigDocument.class);
        config.withConfigurer(this.configurerProvider.get());
        config.getConfigurer().setRegistry(this.transformerRegistry);
        config.setSaver(document -> this.write(collection, path, document));
        return config;
    }

    public abstract ConfigDocument read(ConfigDocument document, PersistenceCollection collection, PersistencePath path);
}

