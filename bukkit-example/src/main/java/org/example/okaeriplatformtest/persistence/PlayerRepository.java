package org.example.okaeriplatformtest.persistence;

import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.repository.DocumentRepository;
import eu.okaeri.persistence.repository.annotation.DocumentCollection;
import eu.okaeri.persistence.repository.annotation.DocumentIndex;
import eu.okaeri.persistence.repository.annotation.DocumentPath;
import eu.okaeri.platform.core.annotation.DependsOn;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

// required to auto register the repository
// with correct persistence
@DependsOn(
    type = DocumentPersistence.class,
    name = "persistence"
)
// example flat/database persistence with custom object
// note that saving can be done using PlayerPersistence#save()
// and DocumentRepository#save is not required
// DocumentRepository provides multiple default methods
// to create, read, update and delete entities
@DocumentCollection(path = "player", keyLength = 36, indexes = {
    @DocumentIndex(path = "name", maxLength = 24),
    @DocumentIndex(path = "lastJoinedLocation.world", maxLength = 64)
})
public interface PlayerRepository extends DocumentRepository<UUID, PlayerProperties> {

    // find by property allows to run an easy search
    // optimized for use with indexed properties
    // superior to filtering result of findAll by hand
    // can scan non-indexed fields using stream
    // filters or platform specific implementation
    @DocumentPath("name")
    Optional<PlayerProperties> findByName(String name);

    @DocumentPath("lastJoinedLocation.world")
    Stream<PlayerProperties> findByLastJoinedLocationWorld(String name);

    @DocumentPath("lastJoinedLocation.y")
    Stream<PlayerProperties> findByLastJoinedLocationY(int y);

    // custom methods may be implemented
    default PlayerProperties get(OfflinePlayer player) {

        // load properties from the storage backend
        PlayerProperties properties = this.findOrCreateByPath(player.getUniqueId());

        // update basic properties
        // name is the most important here as it can be changed
        if (player.getName() != null) {
            properties.setName(player.getName());
        }

        // ready to use object, remember to read all the notes here
        return properties;
    }
}
