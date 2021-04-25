package eu.okaeri.platform.persistence.redis;

import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.Collection;
import java.util.stream.Collectors;

public class BasicRedisPersistence extends ConfigPersistence {

    private StatefulRedisConnection<String, String> connection;

    public BasicRedisPersistence(PersistencePath basePath, RedisClient client, Configurer configurer, OkaeriSerdesPack... serdesPacks) {
        super(basePath, configurer, serdesPacks);
        this.connection = client.connect();
    }

    @Override
    public Collection<ConfigDocument> readAll(PersistencePath collection) {
        return this.connection.sync().hkeys(this.getBasePath().sub(collection).getValue()).stream()
                .map(key -> this.read(collection, PersistencePath.of(key)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(PersistencePath collection, PersistencePath path) {
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hexists(hKey, path.getValue());
    }

    @Override
    public boolean write(PersistencePath collection, PersistencePath path, ConfigDocument document) {
        String hKey = this.getBasePath().sub(collection).getValue();
        this.connection.sync().hset(hKey, path.getValue(), document.saveToString());
        return true;
    }

    @Override
    public ConfigDocument load(ConfigDocument document, PersistencePath collection, PersistencePath path) {

        String hKey = this.getBasePath().sub(collection).getValue();
        String configContents = this.connection.sync().hget(hKey, path.getValue());

        if (configContents == null) {
            return document;
        }

        return (ConfigDocument) document.load(configContents);
    }
}
