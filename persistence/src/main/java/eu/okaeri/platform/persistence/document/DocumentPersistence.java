package eu.okaeri.platform.persistence.document;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.TransformerRegistry;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.platform.persistence.Persistence;
import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.raw.RawPersistence;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentPersistence implements Persistence<Document> {

    @Getter private final ConfigurerProvider configurerProvider;
    @Getter private final OkaeriSerdesPack[] serdesPacks;
    @Getter private final RawPersistence raw;
    private TransformerRegistry transformerRegistry;

    @Override
    public void registerCollection(PersistenceCollection collection) {
        this.getRaw().registerCollection(collection);
    }

    @Override
    public PersistencePath getBasePath() {
        return this.getRaw().getBasePath();
    }

    public DocumentPersistence(RawPersistence rawPersistence, ConfigurerProvider configurerProvider, OkaeriSerdesPack... serdesPacks) {
        this.raw = rawPersistence;
        this.configurerProvider = configurerProvider;
        this.serdesPacks = serdesPacks;
        this.transformerRegistry = new TransformerRegistry();
        Stream.concat(Stream.of(new StandardSerdes(), new SerdesCommons()), Stream.of(this.serdesPacks)).forEach(pack -> pack.register(this.transformerRegistry));
    }

    @Override
    public Document read(PersistenceCollection collection, PersistencePath path) {
        Document document = this.createDocument(collection, path);
        String read = this.getRaw().read(collection, path);
        if (read == null) return document;
        return (Document) document.load(read);
    }

    @Override
    public Map<PersistencePath, Document> readAll(PersistenceCollection collection) {
        return this.getRaw().readAll(collection).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    Document document = this.createDocument(collection, entry.getKey());
                    return (Document) document.load(entry.getValue());
                }));
    }

    @Override
    public boolean exists(PersistenceCollection collection, PersistencePath path) {
        return this.getRaw().exists(collection, path);
    }

    @Override
    public boolean write(PersistenceCollection collection, PersistencePath path, Document document) {
        return this.getRaw().write(collection, path, document.saveToString());
    }

    @Override
    public boolean delete(PersistenceCollection collection, PersistencePath path) {
        return this.getRaw().delete(collection, path);
    }

    @Override
    public boolean deleteAll(PersistenceCollection collection) {
        return this.getRaw().deleteAll(collection);
    }

    @Override
    public long deleteAll() {
        return this.getRaw().deleteAll();
    }

    public Document createDocument(PersistenceCollection collection, PersistencePath path) {
        this.getRaw().checkCollectionRegistered(collection);
        Document config = ConfigManager.create(Document.class);
        config.withConfigurer(this.configurerProvider.get());
        config.getConfigurer().setRegistry(this.transformerRegistry);
        config.setSaver(document -> this.write(collection, path, document));
        return config;
    }
}
