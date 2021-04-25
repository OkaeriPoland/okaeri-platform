package eu.okaeri.platform.persistence.redis;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigConfigurerProvider;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.stream.Collectors;

public class BasicRedisPersistence extends ConfigPersistence {

    @Getter private StatefulRedisConnection<String, String> connection;

    public BasicRedisPersistence(PersistencePath basePath, RedisClient client, ConfigConfigurerProvider configurerProvider, OkaeriSerdesPack... serdesPacks) {
        super(basePath, configurerProvider, serdesPacks);
        this.connect(client);
    }

    @SneakyThrows
    private void connect(RedisClient client) {
        do {
            try {
                this.connection = client.connect();
            } catch (Exception exception) {
                if (exception.getCause() != null) {
                    System.out.println("Cannot connect with redis (waiting 30s): " + exception.getMessage() + " caused by " + exception.getCause().getMessage());
                } else {
                    System.out.println("Cannot connect with redis (waiting 30s): " + exception.getMessage());
                }
                Thread.sleep(30_000);
            }
        } while (this.connection == null);
    }

    @Override
    public Collection<ConfigDocument> readAll(PersistencePath collection) {
        this.checkCollectionRegistered(collection);
        return this.connection.sync().hgetall(this.getBasePath().sub(collection).getValue()).entrySet().stream()
                .map(entry -> {
                    PersistencePath path = PersistencePath.of(entry.getKey());
                    return (ConfigDocument) this.createDocument(collection, path).load(entry.getValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(PersistencePath collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hexists(hKey, path.getValue());
    }

    @Override
    public boolean write(PersistencePath collection, PersistencePath path, ConfigDocument document) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        this.connection.sync().hset(hKey, path.getValue(), document.saveToString());
        return true;
    }

    @Override
    public ConfigDocument load(ConfigDocument document, PersistencePath collection, PersistencePath path) {

        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        String configContents = this.connection.sync().hget(hKey, path.getValue());

        if (configContents == null) {
            return document;
        }

        return (ConfigDocument) document.load(configContents);
    }

    @Override
    public boolean delete(PersistencePath collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hdel(hKey, path.getValue()) > 0;
    }

    @Override
    public boolean deleteAll(PersistencePath collection) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().del(hKey) > 0;
    }

    @Override
    public long deleteAll() {
        return this.connection.sync().del(this.getKnownCollections().toArray(new String[0]));
    }
}
