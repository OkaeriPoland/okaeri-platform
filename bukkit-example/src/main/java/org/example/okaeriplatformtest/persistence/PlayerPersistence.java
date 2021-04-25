package org.example.okaeriplatformtest.persistence;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
// example flat persistence with custom object
// note that saving is done using CustomObject#save() and not BasicFlatPersistence#write
public class PlayerPersistence {

    private static final PersistencePath COLLECTION = PersistencePath.of("player");
    @Inject private ConfigPersistence persistence;

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
        return this.persistence.readAll(COLLECTION).stream()
                .map(document -> document.into(PlayerProperties.class))
                .collect(Collectors.toList());
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
        ConfigDocument configDocument = this.persistence.read(COLLECTION, PersistencePath.of(uniqueId));
        PlayerProperties properties = configDocument.into(PlayerProperties.class);

        // update basic properties
        // name is the most important here as it can be changed
        properties.setUuid(uniqueId);
        if (playerName != null) properties.setName(playerName);

        // ready to use object, remember to read all the notes here
        return properties;
    }
}
