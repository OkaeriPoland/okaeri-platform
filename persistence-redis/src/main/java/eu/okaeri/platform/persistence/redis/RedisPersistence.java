package eu.okaeri.platform.persistence.redis;

import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistenceEntity;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.raw.RawPersistence;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RedisPersistence extends RawPersistence {

    private static final Logger LOGGER = Logger.getLogger(RedisPersistence.class.getName());
    @Getter private StatefulRedisConnection<String, String> connection;

    public RedisPersistence(PersistencePath basePath, RedisClient client) {
        super(basePath, false, false, true);
        this.connect(client);
    }

    @SneakyThrows
    private void connect(RedisClient client) {
        do {
            try {
                this.connection = client.connect();
            } catch (Exception exception) {
                if (exception.getCause() != null) {
                    LOGGER.severe("Cannot connect with redis (waiting 30s): " + exception.getMessage() + " caused by " + exception.getCause().getMessage());
                } else {
                    LOGGER.severe("Cannot connect with redis (waiting 30s): " + exception.getMessage());
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
        String hKey = this.getBasePath().sub(collection).getValue();
        return this.connection.sync().hgetall(hKey).entrySet().stream()
                .collect(Collectors.toMap(entry -> PersistencePath.of(entry.getKey()), Map.Entry::getValue));
    }

    @Override
    public Stream<PersistenceEntity<String>> streamAll(PersistenceCollection collection) {

        this.checkCollectionRegistered(collection);
        RedisCommands<String, String> sync = this.connection.sync();
        String hKey = this.getBasePath().sub(collection).getValue();

        long totalKeys = sync.hlen(hKey);
        long step = totalKeys / 100;
        if (step < 50) {
            step = 50;
        }

        ScanIterator<KeyValue<String, String>> iterator = ScanIterator.hscan(sync, hKey, ScanArgs.Builder.limit(step));
        return StreamSupport.stream(Spliterators.spliterator(new Iterator<PersistenceEntity<String>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public PersistenceEntity<String> next() {
                KeyValue<String, String> next = iterator.next();
                return new PersistenceEntity<>(PersistencePath.of(next.getKey()), next.getValue());
            }
        }, totalKeys, Spliterator.NONNULL), false);
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
