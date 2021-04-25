package eu.okaeri.platform.persistence.flat;

import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.config.ConfigConfigurerProvider;
import eu.okaeri.platform.persistence.config.ConfigDocument;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Collection<ConfigDocument> readAll(PersistencePath collection) {

        this.checkCollectionRegistered(collection);
        File collectionFile = this.getBasePath().sub(collection).toFile();
        File[] files = collectionFile.listFiles();

        if (files == null) {
            return Collections.emptySet();
        }

        return Arrays.stream(files)
                .map(file -> {
                    PersistencePath path = PersistencePath.of(file.getName().substring(0, file.getName().length() - this.getFileSuffix().length()));
                    return (ConfigDocument) this.createDocument(collection, path).load(file);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(PersistencePath collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.toFullPath(collection, path).toFile().exists();
    }

    @Override
    public boolean write(PersistencePath collection, PersistencePath path, ConfigDocument document) {
        this.checkCollectionRegistered(collection);
        document.save(this.toFullPath(collection, path).toFile());
        return true;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigDocument createDocument(PersistencePath collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
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
        this.checkCollectionRegistered(collection);
        return (ConfigDocument) document.load();
    }

    @Override
    public boolean delete(PersistencePath collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.toFullPath(collection, path).toFile().delete();
    }

    @Override
    @SneakyThrows
    public boolean deleteAll(PersistencePath collection) {

        this.checkCollectionRegistered(collection);
        File collectionFile = this.getBasePath().sub(collection).toFile();

        return collectionFile.exists() && (this.delete(collectionFile) > 0);
    }

    @Override
    public long deleteAll() {

        File[] files = this.getBasePath().toFile().listFiles();
        if (files == null) {
            return 0;
        }

        return Arrays.stream(files)
                .filter(file -> this.getKnownCollections().contains(file.getName()))
                .map(this::delete)
                .filter(deleted -> deleted > 0)
                .count();
    }

    @SneakyThrows
    private long delete(File file) {
        try (Stream<Path> walk = Files.walk(file.toPath())) {
            return walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .map(File::delete)
                    .filter(Predicate.isEqual(true))
                    .count();
        }
    }
}
