package eu.okaeri.platform.persistence.flat;

import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;

// build to store data in the PLUGIN_DIR/storage/*
public class BasicFlatPersistence extends ConfigPersistence {

    @Getter private final PersistencePath basePath;
    @Getter private final String fileSuffix;

    public BasicFlatPersistence(File basePath, String fileSuffix, Configurer configurer, OkaeriSerdesPack... serdesPacks) {
        super(configurer, serdesPacks);
        this.basePath = PersistencePath.of(basePath);
        this.fileSuffix = fileSuffix;
    }

    @Override
    public boolean exists(PersistencePath path) {
        return this.toFullPath(path).toFile().exists();
    }

    @Override
    public boolean write(PersistencePath path, ConfigDocument document) {
        document.save(this.toFullPath(path).toFile());
        return true;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void documentCreated(ConfigDocument document, PersistencePath fullPath) {
        // create parent dir
        File dir = fullPath.group().toFile();
        dir.mkdirs();
        // create empty file
        File bindFile = fullPath.toFile();
        bindFile.createNewFile();
        // add bindFile
        document.withBindFile(fullPath.toFile());
    }

    @Override
    public PersistencePath convertPath(PersistencePath path) {
        return path.append(this.fileSuffix);
    }

    @Override
    public ConfigDocument load(ConfigDocument document, PersistencePath fullPath) {
        return (ConfigDocument) document.load();
    }
}
