package eu.okaeri.platform.persistence.redis;

import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.raw.RawPersistence;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.stream.Collectors;

public class RedisPersistence extends RawPersistence {

    @Getter private StatefulRedisConnection<String, String> connection;

    public RedisPersistence(PersistencePath basePath, RedisClient client) {
        super(basePath);
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
    public String read(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hget(hKey, path.getValue());
    }

    @Override
    public Map<PersistencePath, String> readAll(PersistenceCollection collection) {
        this.checkCollectionRegistered(collection);
        return this.connection.sync().hgetall(this.getBasePath().sub(collection).getValue()).entrySet().stream()
                .collect(Collectors.toMap(entry -> PersistencePath.of(entry.getKey()), Map.Entry::getValue));
    }

    @Override
    public boolean exists(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hexists(hKey, path.getValue());
    }

    @Override
    public boolean write(PersistenceCollection collection, PersistencePath path, String raw) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        this.connection.sync().hset(hKey, path.getValue(), raw);
        return true;
    }

    @Override
    public boolean delete(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hdel(hKey, path.getValue()) > 0;
    }

    @Override
    public boolean deleteAll(PersistenceCollection collection) {
        this.checkCollectionRegistered(collection);
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().del(hKey) > 0;
    }

    @Override
    public long deleteAll() {
        return this.connection.sync().del(this.getKnownCollections().keySet().toArray(new String[0]));
    }
}
