package org.example.okaeriplatformtest.persistence;

import eu.okaeri.persistence.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
// custom properties object based on okaeri-configs
// allows for easy access to player's properties with
// getters/setters instead of raw values and string keys
public class PlayerProperties extends Document {

    private String name;
    private Instant lastJoined;

    public UUID getUniqueId() {
        return this.getPath().toUUID();
    }
}
