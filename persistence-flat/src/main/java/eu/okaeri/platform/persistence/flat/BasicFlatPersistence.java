package eu.okaeri.platform.persistence.flat;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigConfigurerProvider;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Arrays;

// build to store data in the PLUGIN_DIR/storage/*
public class BasicFlatPersistence extends ConfigPersistence {

    @Getter private final PersistencePath basePath;
    @Getter private final String fileSuffix;

    public BasicFlatPersistence(File basePath, String fileSuffix, ConfigConfigurerProvider configurerProvider, OkaeriSerdesPack... serdesPacks) {
        super(PersistencePath.of(basePath), configurerProvider, serdesPacks);
        this.basePath = PersistencePath.of(basePath);
        this.fileSuffix = fileSuffix;
    }

    @Override
    public boolean exists(PersistencePath collection, PersistencePath path) {
        return this.toFullPath(collection, path).toFile().exists();
    }

    @Override
    public boolean write(PersistencePath collection, PersistencePath path, ConfigDocument document) {
        document.save(this.toFullPath(collection, path).toFile());
        return true;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigDocument createDocument(PersistencePath collection, PersistencePath path) {
        // create full path
        PersistencePath fullPath = this.toFullPath(collection, path);
        // create parent dir
        File dir = fullPath.group().toFile();
        dir.mkdirs();
        // create empty file
        File bindFile = fullPath.toFile();
        bindFile.createNewFile();
        // create document and add bindFile
        ConfigDocument document = super.createDocument(collection, path);
        document.withBindFile(fullPath.toFile());
        return document;
    }

    @Override
    public PersistencePath convertPath(PersistencePath path) {
        return path.append(this.fileSuffix);
    }

    @Override
    public ConfigDocument load(ConfigDocument document, PersistencePath collection, PersistencePath path) {
        return (ConfigDocument) document.load();
    }

    @Override
    public boolean delete(PersistencePath collection, PersistencePath path) {
        return this.toFullPath(collection, path).toFile().delete();
    }

    @Override
    public boolean deleteAll(PersistencePath collection) {
        return this.delete(this.getBasePath().sub(collection)) > 0;
    }

    @Override
    public long deleteAll() {
        return this.delete(this.getBasePath());
    }

    private long delete(PersistencePath path) {

        File[] files = path.toFile().listFiles();
        if (files == null) {
            return 0;
        }

        return Arrays.stream(files)
                .map(File::delete)
                .count();
    }
}
