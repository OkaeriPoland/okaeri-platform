package org.example.okaeriplatformtest.persistence;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.commons.persistence.YamlBukkitPersistence;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.core.persistence.PersistencePath;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Component
// example flat persistence with custom object
// note that saving is done using CustomObject#save() and not BasicFlatPersistence#write
public class PlayerPersistence {

    @Inject private YamlBukkitPersistence persistence;

    // make sure the method incorporates here some form of caching, see more below
    public PlayerProperties get(OfflinePlayer player) {
        return this.get(player.getUniqueId(), player.getName());
    }

    // useful check in some places, because get is expected
    // to return empty properties instead of throwing exception
    public boolean exists(OfflinePlayer player) {
        return this.persistence.exists(this.toPath(player.getUniqueId()));
    }

    // for the real use case it is recommended
    // to cache PlayerPersistence#get methods
    // calling #read/#readInto causes disk access, etc.
    //
    // one may use Map<UUID, PlayerProperties>
    // with listeners for join/quit
    //
    private PlayerProperties get(UUID uniqueId, String playerName) {

        // load properties from the storage backend
        PersistencePath persistencePath = this.toPath(uniqueId);
        PlayerProperties properties = this.persistence.readInto(persistencePath, PlayerProperties.class);

        // update basic properties
        // name is the most important here as it can be changed
        properties.setUuid(uniqueId);
        if (playerName != null) properties.setName(playerName);

        // ready to use object, remember to read all the notes here
        return properties;
    }

    // player:UUID
    private PersistencePath toPath(UUID playerId) {
        return PersistencePath.of("player").sub(playerId);
    }
}
