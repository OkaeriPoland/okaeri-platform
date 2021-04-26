package eu.okaeri.platform.persistence.raw;

import eu.okaeri.platform.persistence.Persistence;
import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistencePath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public abstract class RawPersistence implements Persistence<String> {

    @Getter private final PersistencePath basePath;
    @Getter private final Map<String, PersistenceCollection> knownCollections = new HashMap<>();

    @Override
    public void registerCollection(PersistenceCollection collection) {
        this.knownCollections.put(collection.getValue(), collection);
    }

    public void checkCollectionRegistered(PersistenceCollection collection) {
        if (this.knownCollections.containsKey(collection.getValue())) {
            return;
        }
        throw new IllegalArgumentException("cannot use unregistered collection: " + collection);
    }

    public PersistencePath toFullPath(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.getBasePath().sub(collection).sub(this.convertPath(path));
    }

    public PersistencePath convertPath(PersistencePath path) {
        return path;
    }
}

