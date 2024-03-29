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
public class TestConfig extends OkaeriConfig {

    @Size(min = 1, max = 64) // validation using okaeri-validator
    @Variable("OPE_GREETING") // use jvm property environment variable if available
    @Comment("Example config value") // built-in comment support
    private String greeting = "Hi!!!!!!!!1111oneone"; // default values

    @Comment("Example command")
    private String repeatingCommand = "say from the config for {name}!";

    @Comment("Storage settings")
    private StorageConfig storage = new StorageConfig();

    @Getter
    @Setter
    public static class StorageConfig extends OkaeriConfig {

        @Variable("OPE_STORAGE_BACKEND")
        @Comment("Type of the storage backend: FLAT, REDIS, MONGO, MYSQL, H2")
        private StorageBackend backend = StorageBackend.FLAT;

        @Variable("OPE_STORAGE_PREFIX")
        @Comment("Prefix for the storage: allows to have multiple instances using same database")
        @Comment("FLAT   : no effect due to local nature")
        @Comment("REDIS  : {storagePrefix}:{collection} -> ope:player")
        @Comment("MONGO  : {storagePrefix}:{collection} -> ope_player")
        @Comment("MYSQL  : {storagePrefix}:{collection} -> ope_player")
        @Comment("H2     : {storagePrefix}:{collection} -> ope_player")
        private String prefix = "ope";

        @Variable("OPE_STORAGE_URI")
        @Comment("FLAT   : not applicable, plugin controlled")
        @Comment("REDIS  : redis://localhost")
        @Comment("MONGO  : mongodb://localhost:27017/db")
        @Comment("MYSQL  : jdbc:mysql://localhost:3306/db?user=root&password=1234")
        @Comment("H2     : jdbc:h2:file:./plugins/OkaeriPlatformBukkitExample/storage;mode=mysql")
        private String uri = "redis://localhost";
    }
}
