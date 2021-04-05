package org.example.okaeriplatformtest.persistence;

import eu.okaeri.configs.OkaeriConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
// custom properties object based on okaeri-configs
// allows for easy access to player's properties with
// getters/setters instead of raw values and string keys
public class PlayerProperties extends OkaeriConfig {

    // recommended defaults
    private UUID uuid;
    private String name;

    // custom values
    private String lastJoined;
}
