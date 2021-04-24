package eu.okaeri.platform.persistence.redis;

import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

public abstract class BasicRedisPersistence extends ConfigPersistence {

    private StatefulRedisConnection<String, String> connection;

    public BasicRedisPersistence(RedisClient client, Configurer configurer, OkaeriSerdesPack[] serdesPacks) {
        super(configurer, serdesPacks);
        this.connection = client.connect();
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
        return (ConfigDocument) document.load((configContents == null) ? "" : configContents);
    }
}
