package org.example.okaeriplatformtest.persistence;

import eu.okaeri.platform.persistence.config.ConfigDocument;
import lombok.*;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
// custom properties object based on okaeri-configs
// allows for easy access to player's properties with
// getters/setters instead of raw values and string keys
public class PlayerProperties extends ConfigDocument {

    // recommended defaults
    private UUID uuid;
    private String name;

    // custom values
    private String lastJoined;
}
