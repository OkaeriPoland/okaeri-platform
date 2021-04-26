package eu.okaeri.platform.persistence.flat;

import eu.okaeri.platform.persistence.PersistenceCollection;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.PersistenceEntity;
import eu.okaeri.platform.persistence.raw.RawPersistence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FlatPersistence extends RawPersistence {

    @Getter private final PersistencePath basePath;
    @Getter private final String fileSuffix;

    public FlatPersistence(File basePath, String fileSuffix) {
        super(PersistencePath.of(basePath));
        this.basePath = PersistencePath.of(basePath);
        this.fileSuffix = fileSuffix;
    }

    @Override
    public String read(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        File file = this.toFullPath(collection, path).toFile();
        return this.fileToString(file);
    }

    @Override
    public Map<PersistencePath, String> readAll(PersistenceCollection collection) {

        this.checkCollectionRegistered(collection);
        File collectionFile = this.getBasePath().sub(collection).toFile();
        File[] files = collectionFile.listFiles();
        if (files == null) return Collections.emptyMap();

        return Arrays.stream(files)
                .map(file -> {
                    PersistencePath path = PersistencePath.of(file.getName().substring(0, file.getName().length() - this.getFileSuffix().length()));
                    return new Pair<>(path, this.fileToString(file));
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public Stream<PersistenceEntity<String>> streamAll(PersistenceCollection collection) {

        this.checkCollectionRegistered(collection);
        File collectionFile = this.getBasePath().sub(collection).toFile();
        File[] files = collectionFile.listFiles();
        if (files == null) return Stream.of();

        Iterator<File> fileIterator = Arrays.asList(files).iterator();
        return StreamSupport.stream(Spliterators.spliterator(new Iterator<PersistenceEntity<String>>() {
            @Override
            public boolean hasNext() {
                return fileIterator.hasNext();
            }
            @Override
            public PersistenceEntity<String> next() {
                File file = fileIterator.next();
                PersistencePath path = PersistencePath.of(file.getName().substring(0, file.getName().length() - FlatPersistence.this.getFileSuffix().length()));
                return new PersistenceEntity<>(path, FlatPersistence.this.fileToString(file));
            }
        }, files.length, Spliterator.NONNULL), false);
    }

    @Override
    public boolean exists(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.toFullPath(collection, path).toFile().exists();
    }

    @Override
    @SneakyThrows
    public boolean write(PersistenceCollection collection, PersistencePath path, String raw) {
        this.checkCollectionRegistered(collection);
        File file = this.toFullPath(collection, path).toFile();
        this.writeToFile(file, raw);
        return true;
    }

    @Override
    public PersistencePath convertPath(PersistencePath path) {
        return path.append(this.fileSuffix);
    }

    @Override
    public boolean delete(PersistenceCollection collection, PersistencePath path) {
        this.checkCollectionRegistered(collection);
        return this.toFullPath(collection, path).toFile().delete();
    }

    @Override
    @SneakyThrows
    public boolean deleteAll(PersistenceCollection collection) {

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
                .filter(file -> this.getKnownCollections().keySet().contains(file.getName()))
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

    @Data
    @AllArgsConstructor
    private class Pair<L, R> {
        private L left;
        private R right;
    }

    @SneakyThrows
    private String fileToString(File file) {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private void writeToFile(File file, String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(text);
        }
    }
}
