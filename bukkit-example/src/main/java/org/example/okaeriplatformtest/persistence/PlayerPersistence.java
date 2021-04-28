package org.example.okaeriplatformtest.persistence;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.persistence.PersistenceCollection;
import eu.okaeri.persistence.PersistenceEntity;
import eu.okaeri.persistence.PersistencePath;
import eu.okaeri.persistence.document.Document;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.index.IndexProperty;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
// example flat/database persistence with custom object
// note that saving can be done using PlayerPersistence#save()
// and BasicFlatPersistence#write is not required
public class PlayerPersistence {

    private static final PersistenceCollection COLLECTION = PersistenceCollection.of("player", 36)
            .index(IndexProperty.of("name", 24))
            .index(IndexProperty.parse("lastJoinedLocation.world").maxLength(64));
    @Inject private DocumentPersistence persistence;

    // collection MUST be registered to use
    // this is to allow optimized queries
    @PostConstruct
    private void init() {
        this.persistence.registerCollection(COLLECTION);
    }

    // make sure the method incorporates here some form of caching, see more below
    public PlayerProperties get(OfflinePlayer player) {
        return this.get(player.getUniqueId(), player.getName());
    }

    // useful check in some places, because get is expected
    // to return empty properties instead of throwing exception
    public boolean exists(OfflinePlayer player) {
        return this.persistence.exists(COLLECTION, PersistencePath.of(player.getUniqueId()));
    }

    // accessing all entries is greatly expensive operation
    // should be generally avoided unless really necessary
    // the time to fetch increases with saved properties count
    public List<PlayerProperties> getAll() {
        return this.persistence.readAll(COLLECTION).values().stream()
                .map(document -> document.into(PlayerProperties.class))
                .collect(Collectors.toList());
    }

    // stream allows to filter/visit elements
    // of whole collection more efficiently
    // using platform specific iterators
    public Stream<PersistenceEntity<PlayerProperties>> stream() {
        return this.persistence.streamAll(COLLECTION).map(entity -> entity.into(PlayerProperties.class));
    }

    // find by property allows to run an easy search
    // optimized for use with indexed properties
    // superior to filtering result of getAll by hand
    // can scan non-indexed fields using stream
    // filters or platform specific implementation
    public Stream<PlayerProperties> findByName(String name) {
        return this.persistence.readByProperty(COLLECTION, PersistencePath.of("name"), name)
                .map(PersistenceEntity::getValue)
                .map(document -> document.into(PlayerProperties.class));
    }

    public Stream<PlayerProperties> findByLastJoinedLocationWorld(String worldName) {
        return this.persistence.readByProperty(COLLECTION, PersistencePath.of("lastJoinedLocation").sub("world"), worldName)
                .map(PersistenceEntity::getValue)
                .map(document -> document.into(PlayerProperties.class));
    }

    // delete properties by player
    public boolean delete(OfflinePlayer player) {
        return this.persistence.delete(COLLECTION, PersistencePath.of(player.getUniqueId()));
    }

    // delete all players properties
    public boolean deleteAll() {
        return this.persistence.deleteAll(COLLECTION);
    }

    // for the real use case it is recommended
    // to cache PlayerPersistence#get methods
    // calling #read causes disk access, etc.
    //
    // one may use Map<UUID, PlayerProperties>
    // with listeners for join/quit
    //
    private PlayerProperties get(UUID uniqueId, String playerName) {

        // load properties from the storage backend
        Document configDocument = this.persistence.read(COLLECTION, PersistencePath.of(uniqueId));
        PlayerProperties properties = configDocument.into(PlayerProperties.class);

        // update basic properties
        // name is the most important here as it can be changed
        properties.setUuid(uniqueId);
        if (playerName != null) properties.setName(playerName);

        // ready to use object, remember to read all the notes here
        return properties;
    }
}
