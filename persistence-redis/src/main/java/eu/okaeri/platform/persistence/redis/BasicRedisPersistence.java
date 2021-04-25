package eu.okaeri.platform.persistence.redis;

import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.Getter;

public class BasicRedisPersistence extends ConfigPersistence {

    @Getter private final PersistencePath basePath;
    private StatefulRedisConnection<String, String> connection;

    public BasicRedisPersistence(PersistencePath basePath, RedisClient client, Configurer configurer, OkaeriSerdesPack... serdesPacks) {
        super(configurer, serdesPacks);
        this.connection = client.connect();
        this.basePath = basePath;
    }

    @Override
    public boolean exists(PersistencePath path) {
        return this.connection.sync().exists(this.toFullPath(path).getValue()) == 1;
    }

    @Override
    public boolean write(PersistencePath path, ConfigDocument document) {
        this.connection.sync().set(this.toFullPath(path).getValue(), document.saveToString());
        return true;
    }

    @Override
    public ConfigDocument load(ConfigDocument document, PersistencePath fullPath) {
        String configContents = this.connection.sync().get(fullPath.getValue());
        if (configContents == null) return document;
        return (ConfigDocument) document.load(configContents);
    }
}
