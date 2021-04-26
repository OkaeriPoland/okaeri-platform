package org.example.okaeriplatformtest.persistence;

import eu.okaeri.platform.persistence.document.Document;
import lombok.*;
import org.bukkit.Location;

import java.time.Instant;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
// custom properties object based on okaeri-configs
// allows for easy access to player's properties with
// getters/setters instead of raw values and string keys
public class PlayerProperties extends Document {

    // recommended defaults
    private UUID uuid;
    private String name;

    // custom values
    private Instant lastJoined;
    private Location lastJoinedLocation;
}
