package org.example.okaeriplatformtest.persistence;

import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.repository.DocumentRepository;
import eu.okaeri.persistence.repository.annotation.DocumentCollection;
import eu.okaeri.persistence.repository.annotation.DocumentIndex;
import eu.okaeri.persistence.repository.annotation.DocumentPath;
import eu.okaeri.platform.core.annotation.DependsOn;

import java.util.Optional;
import java.util.UUID;

// required to auto register the repository
// with correct persistence
@DependsOn(
    type = DocumentPersistence.class,
    name = "persistence"
)
// example flat/database persistence with custom object
// note that saving can be done using Access#save()
// and DocumentRepository#save is not required
// DocumentRepository provides multiple default methods
// to create, read, update and delete entities
@DocumentCollection(path = "access", keyLength = 36, indexes = {
    @DocumentIndex(path = "token", maxLength = 64)
})
public interface AccessRepository extends DocumentRepository<UUID, Access> {

    // find by property allows to run an easy search
    // optimized for use with indexed properties
    // superior to filtering result of findAll by hand
    // can scan non-indexed fields using stream
    // filters or platform specific implementation
    @DocumentPath("token")
    Optional<Access> findByToken(String token);
}
