package eu.okaeri.platform.persistence.redis;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigConfigurerProvider;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BasicRedisPersistence extends ConfigPersistence {

    private StatefulRedisConnection<String, String> connection;
    private Set<String> knownCollections = new HashSet<>();

    private void watchKnownCollection(PersistencePath collection) {
        if (this.knownCollections.isEmpty()) {
            this.knownCollections = new HashSet<>(this.connection.sync().smembers(this.getBasePath().getValue()));
        }
        if (collection != null) {
            String collectionName = collection.getValue();
            if (!this.knownCollections.add(collectionName)) {
                return;
            }
            this.connection.sync().sadd(this.getBasePath().getValue(), collectionName);
        }
    }

    public BasicRedisPersistence(PersistencePath basePath, RedisClient client, ConfigConfigurerProvider configurerProvider, OkaeriSerdesPack... serdesPacks) {
        super(basePath, configurerProvider, serdesPacks);
        this.connection = client.connect();
        this.watchKnownCollection(null);
    }

    @Override
    public Collection<ConfigDocument> readAll(PersistencePath collection) {
        this.watchKnownCollection(collection);
        return this.connection.sync().hgetall(this.getBasePath().sub(collection).getValue()).entrySet().stream()
                .map(entry -> {
                    PersistencePath path = PersistencePath.of(entry.getKey());
                    return (ConfigDocument) this.createDocument(collection, path).load(entry.getValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(PersistencePath collection, PersistencePath path) {
        this.watchKnownCollection(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hexists(hKey, path.getValue());
    }

    @Override
    public boolean write(PersistencePath collection, PersistencePath path, ConfigDocument document) {
        this.watchKnownCollection(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        this.connection.sync().hset(hKey, path.getValue(), document.saveToString());
        return true;
    }

    @Override
    public ConfigDocument load(ConfigDocument document, PersistencePath collection, PersistencePath path) {

        this.watchKnownCollection(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        String configContents = this.connection.sync().hget(hKey, path.getValue());

        if (configContents == null) {
            return document;
        }

        return (ConfigDocument) document.load(configContents);
    }

    @Override
    public boolean delete(PersistencePath collection, PersistencePath path) {
        this.watchKnownCollection(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hdel(hKey, path.getValue()) > 0;
    }

    @Override
    public boolean deleteAll(PersistencePath collection) {
        this.watchKnownCollection(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().del(hKey) > 0;
    }

    @Override
    public long deleteAll() {
        this.watchKnownCollection(null);
        return this.connection.sync().del(this.knownCollections.toArray(new String[0]));
    }
}
