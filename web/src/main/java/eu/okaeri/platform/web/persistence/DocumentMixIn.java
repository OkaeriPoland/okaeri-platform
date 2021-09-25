package eu.okaeri.platform.web.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.okaeri.persistence.PersistenceCollection;
import eu.okaeri.persistence.PersistencePath;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.document.DocumentSaver;

public abstract class DocumentMixIn {

    @JsonIgnore private DocumentSaver saver;
    @JsonIgnore private DocumentPersistence persistence;
    @JsonIgnore private PersistenceCollection collection;

    @JsonProperty(value = "id", index = 0)
    @JsonSerialize(using = PersistencePathSerializer.class)
    @JsonDeserialize(using = PersistencePathDeserializer.class)
    private PersistencePath path;
}
