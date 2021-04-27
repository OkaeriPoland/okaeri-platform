package eu.okaeri.platform.persistence.document;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.TransformerRegistry;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.platform.persistence.Persistence;
import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistenceEntity;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.index.IndexProperty;
import eu.okaeri.platform.persistence.raw.RawPersistence;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentPersistence implements Persistence<Document> {

    private static final Logger LOGGER = Logger.getLogger(DocumentPersistence.class.getName());

    @Getter private final ConfigurerProvider configurerProvider;
    @Getter private final OkaeriSerdesPack[] serdesPacks;
    @Getter private final RawPersistence raw;
    private TransformerRegistry transformerRegistry;
    private Configurer simplifier;

    public DocumentPersistence(RawPersistence rawPersistence, ConfigurerProvider configurerProvider, OkaeriSerdesPack... serdesPacks) {
        this.raw = rawPersistence;
        this.configurerProvider = configurerProvider;
        this.serdesPacks = serdesPacks;
        // shared transform registry for faster transformations
        this.transformerRegistry = new TransformerRegistry();
        Stream.concat(Stream.of(new StandardSerdes(), new SerdesCommons()), Stream.of(this.serdesPacks)).forEach(pack -> pack.register(this.transformerRegistry));
        // simplifier for document mappings
        this.simplifier = configurerProvider.get();
        this.simplifier.setRegistry(this.transformerRegistry);
    }

    @Override
    public PersistencePath getBasePath() {
        return this.getRaw().getBasePath();
    }

    @Override
    public void registerCollection(PersistenceCollection collection) {

        this.getRaw().registerCollection(collection);
        Set<IndexProperty> indexes = this.getRaw().getKnownIndexes().getOrDefault(collection, new HashSet<>());
        Set<PersistencePath> withMissingIndexes = this.findMissingIndexes(collection, indexes);

        if (withMissingIndexes.isEmpty()) {
            return;
        }

        int total = withMissingIndexes.size();
        long start = System.currentTimeMillis();
        int updated = 0;
        int every = (total > 1000) ? (total / 20) : 50;
        LOGGER.warning("Found " + total + " entries with invalid indexes, updating..");

        for (PersistencePath key : withMissingIndexes) {
            this.updateIndex(collection, key);
            if ((++updated % every) != 0) {
                continue;
            }
            int percent = (int) (((double) updated / (double) total) * 100);
            LOGGER.warning(updated + " already done (" + percent + "%)");
        }
    }

    @Override
    public boolean updateIndex(PersistenceCollection collection, IndexProperty property, PersistencePath path, String identity) {
        return this.getRaw().updateIndex(collection, property, path, identity);
    }

    @Override
    public boolean updateIndex(PersistenceCollection collection, PersistencePath path, Document document) {

        Set<IndexProperty> collectionIndexes = this.getRaw().getKnownIndexes().get(collection);
        if (collectionIndexes == null) {
            return false;
        }

        Map<String, Object> documentMap = document.asMap(this.simplifier, true);
        int changes = 0;

        for (IndexProperty index : collectionIndexes) {
            Object value = this.extractValue(documentMap, index.toParts());
            if ((value != null) && !this.canUseToString(value)) {
                throw new RuntimeException("cannot transform " + value + " to index as string");
            }
            boolean changed = this.updateIndex(collection, index, path, (value == null) ? null : String.valueOf(value));
            if (changed) changes++;
        }

        return changes > 0;
    }

    @Override
    public boolean updateIndex(PersistenceCollection collection, PersistencePath path) {
        Document document = this.read(collection, path);
        return this.updateIndex(collection, path, document);
    }

    @Override
    public boolean dropIndex(PersistenceCollection collection, IndexProperty property, PersistencePath path) {
        return this.getRaw().dropIndex(collection, property, path);
    }

    @Override
    public boolean dropIndex(PersistenceCollection collection, PersistencePath path) {
        return this.getRaw().dropIndex(collection, path);
    }

    @Override
    public boolean dropIndex(PersistenceCollection collection, IndexProperty property) {
        return this.getRaw().dropIndex(collection, property);
    }

    @Override
    public Set<PersistencePath> findMissingIndexes(PersistenceCollection collection, Set<IndexProperty> indexProperties) {
        return this.getRaw().findMissingIndexes(collection, indexProperties);
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
    public Stream<PersistenceEntity<Document>> readByProperty(PersistenceCollection collection, PersistencePath property, Object propertyValue) {

        if (this.getRaw().isNativeReadByProperty()) {
            return this.getRaw().readByProperty(collection, property, propertyValue).map(this.entityToDocumentMapper(collection));
        }

        List<String> pathParts = property.toParts();
        boolean stringSearch = this.getRaw().isUseStringSearch() && this.canUseToString(propertyValue);

        return this.getRaw().streamAll(collection)
                .filter(entity -> !stringSearch || entity.getValue().contains(String.valueOf(propertyValue)))
                .map(this.entityToDocumentMapper(collection))
                .filter(entity -> {
                    Map<String, Object> document = entity.getValue().asMap(this.simplifier, true);
                    return propertyValue.equals(this.extractValue(document, pathParts));
                });
    }

    @Override
    public Stream<PersistenceEntity<Document>> streamAll(PersistenceCollection collection) {
        return this.getRaw().streamAll(collection).map(this.entityToDocumentMapper(collection));
    }

    @Override
    public boolean exists(PersistenceCollection collection, PersistencePath path) {
        return this.getRaw().exists(collection, path);
    }

    @Override
    public boolean write(PersistenceCollection collection, PersistencePath path, Document document) {
        this.updateIndex(collection, path, document);
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

    private Function<PersistenceEntity<String>, PersistenceEntity<Document>> entityToDocumentMapper(PersistenceCollection collection) {
        return entity -> {
            Document document = this.createDocument(collection, entity.getPath());
            document.load(entity.getValue());
            return entity.into(document);
        };
    }

    private Object extractValue(Map<?, ?> document, List<String> pathParts) {
        for (String part : pathParts) {
            Object element = document.get(part);
            if (element instanceof Map) {
                document = (Map<?, ?>) element;
                continue;
            }
            return element;
        }
        return null;
    }

    private boolean canUseToString(Object value) {
        return (value instanceof String) || (value instanceof Integer) || (value instanceof UUID);
    }
}
