package eu.okaeri.platform.core.persistence.flat;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.core.persistence.Persistence;
import eu.okaeri.platform.core.persistence.PersistencePath;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Collection;

// build to store data in the PLUGIN_DIR/storage/*
public abstract class BasicFlatPersistence implements Persistence<ConfigDocument> {

    @Getter private final Configurer configurer;
    @Getter private final OkaeriSerdesPack[] serdesPacks;
    @Getter private final String fileSuffix;

    public BasicFlatPersistence(Configurer configurer, String fileSuffix, OkaeriSerdesPack... serdesPacks) {
        this.configurer = configurer;
        this.serdesPacks = serdesPacks;
        this.fileSuffix = fileSuffix;
    }

    @Override
    public boolean exists(PersistencePath path) {
        return this.getPath().sub(path.append(this.fileSuffix)).toFile().exists();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigDocument read(PersistencePath path) {
        return new ConfigDocument(this.keyToConfig(path));
    }

    @Override
    public boolean write(PersistencePath key, ConfigDocument document) {
        document.save(key.toFile());
        return true;
    }

    @Override
    public Collection<ConfigDocument> readAll() {
        throw new RuntimeException("Not implemented yet");
    }

    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private ConfigDocument.EmptyConfig keyToConfig(PersistencePath key) {

        PersistencePath fullPath = this.getPath().sub(key.append(this.fileSuffix));
        File dir = fullPath.group().toFile();
        dir.mkdirs();

        File bindFile = fullPath.toFile();
        bindFile.createNewFile();

        return ConfigManager.create(ConfigDocument.EmptyConfig.class, (it) -> {
            it.withConfigurer(this.configurer, this.serdesPacks);
            it.withBindFile(bindFile);
            it.load();
        });
    }
}

