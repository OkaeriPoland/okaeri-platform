package org.example.okaeriplatformtest.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import eu.okaeri.platform.core.annotation.Configuration;
import eu.okaeri.validator.annotation.Size;
import lombok.Getter;
import lombok.Setter;

// automatically created in the plugin dir
// updates comments and changes (new keys) automatically
// manipulate it as pojo and save with #save()
//
// Resulting file:
// # ================================
// #        Magic Configuration
// # ================================
// # Example config value
// greeting: Hi!!!!!!!!1111oneone
//
@Getter
@Setter
// automatically manages file inside plugin's directory
// custom serialization packs with serdes = {CustomSerdesPack.class, ..}
@Configuration
// adds header, supports multiline strings or multiple annotations
// string array can be passed as an argument too, same with @Comment
// it is possible to create empty line with "" and empty comment with " "
@Header("================================")
@Header("       Magic Configuration      ")
@Header("================================")
// automatically applies name transformations (by default)
// config keys can be also individually changed using @CustomKey
// strategies:
// - IDENTITY: do not change (default)
// - SNAKE_CASE: exampleValue -> example_Value
// - HYPHEN_CASE: exampleValue -> example-Value
// modifiers:
// - NONE: do not change (default)
// - TO_LOWER_CASE: e.g. example-Value -> example-value
// - TO_UPPER_CASE: e.g. example_Value -> EXAMPLE_VALUE
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class TestConfig extends OkaeriConfig {

    @Size(min = 1, max = 64) // validation using okaeri-validator
    @Variable("OPE_GREETING") // use jvm property environment variable if available
    @Comment("Example config value") // built-in comment support
    private String greeting = "Hi!!!!!!!!1111oneone"; // default values

    @Comment("Example command")
    private String repeatingCommand = "say from the config for {name}!";

    @Variable("OPE_STORAGE_BACKEND")
    @Comment("Type of the storage backend: FLAT, REDIS")
    private StorageBackend storageBackend = StorageBackend.FLAT;

    @Variable("OPE_STORAGE_PREFIX")
    @Comment("Prefix for the storage: allows to have multiple instances using same database")
    @Comment("FLAT  : no effect due to local nature")
    @Comment("REDIS : {storagePrefix}:{collection} -> OkaeriPlatformBukkitExample:player")
//    @Comment("MYSQL : {storagePrefix}:{collection} -> ope_storage_player (recommended shortened [here 'ope:storage'] due to database limitations)")
    private String storagePrefix = "OkaeriPlatformBukkitExample:storage";

    @Variable("OPE_STORAGE_REDIS_URI")
    @Comment("Redis URI - used for storage-backend: REDIS")
    private String storageRedisUri = "redis://localhost";
}
